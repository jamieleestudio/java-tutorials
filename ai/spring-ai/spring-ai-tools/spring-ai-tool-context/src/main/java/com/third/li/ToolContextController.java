package com.third.li;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 工具上下文（ToolContext + 动态传参）。
 *
 * <p>工具方法可以通过 {@link ToolContext} 访问运行时上下文——比如当前用户、
 * 请求 ID 等，而<b>不需要模型生成</b>这些参数（安全、防注入）。
 *
 * <p>用法：{@code chatClient.prompt(msg).tools(myBean).toolContext(Map.of("userId","alice"))}，
 * 工具方法签名加一个 {@code ToolContext} 参数即可注入。
 */
@RestController
public class ToolContextController {

    private final ChatClient chatClient;

    public ToolContextController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultTools(new UserService())
                .build();
    }

    /** 工具从 ToolContext 读取当前用户，不用模型猜。 */
    @GetMapping("/ai/tool-context")
    public String toolContext(
            @RequestParam(value = "message", defaultValue = "查询当前用户的订单") String message,
            @RequestParam(value = "userId", defaultValue = "alice-001") String userId) {
        return chatClient.prompt(message)
                .toolContext(Map.of("userId", userId, "requestId", "req-" + System.currentTimeMillis()))
                .call()
                .content();
    }

    /** 用户服务工具：从 ToolContext 拿 userId，防止模型伪造。 */
    public static class UserService {

        @Tool(name = "getUserOrders", description = "查询当前用户的订单列表")
        public String getUserOrders(
                @ToolParam(description = "排序方式") String sort,
                ToolContext toolContext) {
            String userId = (String) toolContext.getContext().get("userId");
            return "用户 " + userId + " 的订单（" + sort + "）：订单#1001, #1002, #1003";
        }
    }
}
