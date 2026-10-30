package com.yineng.bpe.distributedsolutions.outbox;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Outbox Demo 启动类。
 * <p>运行前需先启动 MySQL：{@code docker compose up -d}（见模块根目录 docker-compose.yml）。</p>
 * <p>Demo 定义了 {@link OrderEventHandler} Bean（本地业务执行），自动装配中
 * 默认的 {@link LoggingOutboxPublisher} 会自动退位。</p>
 */
@SpringBootApplication
public class OutboxDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(OutboxDemoApplication.class,
                "--outbox.relay.poll-interval=500",
                "--outbox.relay.initial-backoff=1s",
                "--outbox.relay.backoff-multiplier=2.0",
                "--outbox.relay.max-retries=5",
                "--outbox.relay.batch-size=10");
    }

    @Bean
    public OutboxPublisher outboxPublisher() {
        return new OrderEventHandler();
    }
}
