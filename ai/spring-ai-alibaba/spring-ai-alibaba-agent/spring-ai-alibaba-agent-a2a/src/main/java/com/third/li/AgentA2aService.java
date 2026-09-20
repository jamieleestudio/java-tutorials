package com.third.li;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.KeyStrategy;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.agent.a2a.A2aRemoteAgent;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import io.a2a.spec.AgentCapabilities;
import io.a2a.spec.AgentCard;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * A2A 互操作：A2aRemoteAgent 把远程 Agent 当作图节点。
 *
 * <p>A2A（Agent-to-Agent）是跨进程 Agent 协作的开放协议。SAA 的
 * {@link A2aRemoteAgent} 通过远程 Agent 的 AgentCard（自描述元数据，含 endpoint）
 * 把远端 Agent 包装成本地图的一个节点：
 * <ul>
 *   <li>按 url 构建 {@link AgentCard}（也可实现 {@code AgentCardProvider} 动态发现，
 *       或用 a2a-nacos starter 从 Nacos 注册中心拿卡片）</li>
 *   <li>{@code asNode(false, false)} 把远程 Agent 装进本地图 ——
 *       调用方无需关心远端是哪个框架实现的</li>
 * </ul>
 *
 * <p>运行本模块需要一个可达的 A2A server（如 SAA Studio 或任意 A2A 兼容服务），
 * 未启动时接口会返回连接错误提示。
 */
@Service
public class AgentA2aService {

    /** 构建指向远端 AgentCard 的本地图；url 为 A2A server 基地址。 */
    public CompiledGraph buildGraph(String baseUrl) throws GraphStateException {
        AgentCard card = new AgentCard.Builder()
                .name("remote-agent")
                .description("远程 A2A 演示 Agent")
                .url(baseUrl)
                .version("1.0.0")
                .capabilities(new AgentCapabilities(true, false, false, List.of()))
                .defaultInputModes(List.of("text"))
                .defaultOutputModes(List.of("text"))
                .skills(List.of())
                .build();

        A2aRemoteAgent remote = A2aRemoteAgent.builder()
                .name("a2a-remote")
                .description("通过 A2A 协议调用的远程 Agent")
                .agentCard(card)
                .outputKey("answer")
                .build();

        StateGraph graph = new StateGraph("a2a_graph", () -> Map.<String, KeyStrategy>of(
                        "input", new ReplaceStrategy(),
                        "answer", new ReplaceStrategy()))
                // 远程 Agent 作为普通节点接入
                .addNode("remote", remote.asNode(false, false))
                .addEdge(StateGraph.START, "remote")
                .addEdge("remote", StateGraph.END);

        return graph.compile();
    }

    /** 调用远程 Agent：Map 形式返回最终状态。 */
    public Map<String, Object> ask(String baseUrl, String message) throws Exception {
        var result = buildGraph(baseUrl).invoke(java.util.Map.of("input", message));
        return result.map(OverAllState::data).orElse(Map.of());
    }
}
