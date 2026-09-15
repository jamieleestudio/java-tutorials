package com.third.li;

import io.a2a.A2A;
import io.a2a.client.Client;
import io.a2a.client.ClientEvent;
import io.a2a.client.MessageEvent;
import io.a2a.client.TaskEvent;
import io.a2a.client.TaskUpdateEvent;
import io.a2a.client.config.ClientConfig;
import io.a2a.client.transport.jsonrpc.JSONRPCTransport;
import io.a2a.client.transport.jsonrpc.JSONRPCTransportConfigBuilder;
import io.a2a.client.transport.spi.interceptors.ClientCallContext;
import io.a2a.spec.AgentCard;
import io.a2a.spec.Artifact;
import io.a2a.spec.DataPart;
import io.a2a.spec.Message;
import io.a2a.spec.Part;
import io.a2a.spec.Task;
import io.a2a.spec.TextPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * A2A 客户端：调用远端 A2A 智能体。
 *
 * <p>流程（A2A 协议 0.3.x）：
 * <ol>
 *   <li>拉取远端 Agent Card（技能/传输方式）</li>
 *   <li>{@code Client.builder(card).withTransport(JSONRPCTransport.class, ...)} 建客户端</li>
 *   <li>{@code sendMessage(...)} 发送消息；结果通过 consumer 回调（Message 或 Task）</li>
 * </ol>
 *
 * <p><b>踩坑记录</b>：SDK 0.3.2 的 {@code A2A.getAgentCard(...)} 默认去取
 * {@code /.well-known/agent-card.json}，而 Embabel 1.0.0 的服务端注册的是
 * {@code /.well-known/agent.json}（旧路径）——直接用 SDK 的便捷方法会 404。
 * 因此这里用 {@link RestClient} 自己取卡片，再交给 SDK 建客户端。
 *
 * <p>默认指向本应用自己（`demo.remote-a2a-url`），可单进程自包含演示；
 * 换成另一个实例的地址即可做真正的跨进程 Agent 协作。
 */
@Component
public class A2AClient {

    private static final Logger log = LoggerFactory.getLogger(A2AClient.class);

    private final String baseUrl;
    private final RestClient restClient;

    public A2AClient(@Value("${demo.remote-a2a-url}") String baseUrl, RestClient.Builder restClientBuilder) {
        this.baseUrl = baseUrl;
        this.restClient = restClientBuilder.build();
    }

    /** 拉取远端 Agent Card。 */
    public AgentCard agentCard() {
        return restClient.get()
                .uri(a2aBaseUrl() + "/.well-known/agent.json")
                .retrieve()
                .body(AgentCard.class);
    }

    /** 发送一条文本消息给远端智能体，返回其回复文本。 */
    public String send(String text) throws Exception {
        AgentCard card = agentCard();
        List<String> replies = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch done = new CountDownLatch(1);

        Client client = Client.builder(card)
                .withTransport(JSONRPCTransport.class, new JSONRPCTransportConfigBuilder())
                // 关闭流式，让服务端一次返回完整结果（简化演示）
                .clientConfig(ClientConfig.builder().setStreaming(false).setPolling(true).build())
                .addConsumer((event, agentCard) -> {
                    String reply = extractReply(event);
                    if (reply != null && !reply.isBlank()) {
                        replies.add(reply);
                    }
                    if (isTerminal(event)) {
                        done.countDown();
                    }
                })
                .build();

        try {
            log.info("Sending A2A message to {}: {}", card.name(), text);
            client.sendMessage(A2A.toUserMessage(text), (ClientCallContext) null);
            if (!done.await(180, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timed out waiting for A2A response");
            }
        } finally {
            client.close();
        }

        return replies.isEmpty() ? "(远端未返回文本内容)" : String.join("\n", replies);
    }

    private boolean isTerminal(ClientEvent event) {
        if (event instanceof MessageEvent) {
            return true;
        }
        Task task = taskOf(event);
        if (task == null || task.getStatus() == null || task.getStatus().state() == null) {
            return true;
        }
        return task.getStatus().state().isFinal();
    }

    private Task taskOf(ClientEvent event) {
        if (event instanceof TaskEvent taskEvent) {
            return taskEvent.getTask();
        }
        if (event instanceof TaskUpdateEvent updateEvent) {
            return updateEvent.getTask();
        }
        return null;
    }

    /** A2A 基地址（Embabel 把 A2A 端点挂在 /a2a 前缀下）。 */
    private String a2aBaseUrl() {
        return baseUrl.replaceAll("/+$", "") + "/a2a";
    }

    private String extractReply(ClientEvent event) {
        if (event instanceof MessageEvent messageEvent) {
            return textOf(messageEvent.getMessage());
        }
        Task task = taskOf(event);
        if (task != null) {
            log.info("A2A task: state={} artifacts={} history={}",
                    task.getStatus() == null ? null : task.getStatus().state(),
                    task.getArtifacts() == null ? 0 : task.getArtifacts().size(),
                    task.getHistory() == null ? 0 : task.getHistory().size());
            return textOf(task);
        }
        return null;
    }

    private String textOf(Message message) {
        List<String> texts = new ArrayList<>();
        for (Part<?> part : message.getParts()) {
            addText(texts, part);
        }
        return String.join("\n", texts);
    }

    /**
     * 从 Task 提取回复文本：优先 artifacts（真正的产出），
     * 其次 status.message，最后 history 里 agent 的消息。
     * 注意：Embabel 把 Agent 的强类型产出放在 **DataPart**（JSON）里，不是 TextPart。
     */
    private String textOf(Task task) {
        List<String> texts = new ArrayList<>();

        if (task.getArtifacts() != null) {
            for (Artifact artifact : task.getArtifacts()) {
                for (Part<?> part : artifact.parts()) {
                    addText(texts, part);
                }
            }
        }
        if (texts.isEmpty() && task.getStatus() != null && task.getStatus().message() != null) {
            addText(texts, task.getStatus().message());
        }
        if (texts.isEmpty() && task.getHistory() != null) {
            for (Message message : task.getHistory()) {
                if (message.getRole() == Message.Role.AGENT) {
                    addText(texts, message);
                }
            }
        }
        return String.join("\n", texts);
    }

    private void addText(List<String> texts, Message message) {
        for (Part<?> part : message.getParts()) {
            addText(texts, part);
        }
    }

    private void addText(List<String> texts, Part<?> part) {
        if (part instanceof TextPart textPart) {
            texts.add(textPart.getText());
        } else if (part instanceof DataPart dataPart) {
            texts.add(textOfData(dataPart.getData()));
        }
    }

    /** 从结构化数据里挑出可读文本：常见字段优先，必要时递归解包（如 Embabel 的 output 包装）。 */
    private String textOfData(java.util.Map<String, Object> data) {
        if (data == null) {
            return "";
        }
        for (String key : List.of("content", "text", "message", "result", "answer")) {
            Object value = data.get(key);
            if (value != null) {
                return String.valueOf(value);
            }
        }
        for (String key : List.of("output", "data", "payload")) {
            Object value = data.get(key);
            if (value instanceof java.util.Map<?, ?> nested) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> cast = (java.util.Map<String, Object>) nested;
                return textOfData(cast);
            }
        }
        return data.toString();
    }
}
