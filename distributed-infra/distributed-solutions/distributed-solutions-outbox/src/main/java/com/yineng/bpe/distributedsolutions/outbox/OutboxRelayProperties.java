package com.yineng.bpe.distributedsolutions.outbox;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Outbox 定时任务配置项。
 */
@Data
@ConfigurationProperties(prefix = "outbox.relay")
public class OutboxRelayProperties {

    /**
     * 是否启用定时轮询（关闭后 OutboxRelay Bean 仍存在，可手动调用 relayOnce）。
     */
    private boolean enabled = true;

    /**
     * 轮询间隔。
     */
    private Duration pollInterval = Duration.ofSeconds(1);

    /**
     * 每批最多认领的消息数。
     */
    private int batchSize = 50;

    /**
     * 最大重试次数，达到后消息转 FAILED（死信）。
     */
    private int maxRetries = 5;

    /**
     * 首次失败的退避时间，之后按 {@link #backoffMultiplier} 指数增长。
     */
    private Duration initialBackoff = Duration.ofSeconds(5);

    /**
     * 退避倍数。
     */
    private double backoffMultiplier = 2.0;
}
