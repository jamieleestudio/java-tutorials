package com.yineng.bpe.distributedsolutions.outbox;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Outbox 自动装配。
 * <p>业务方定义自己的 {@link OutboxPublisher} Bean 即可替换默认的日志实现，
 * 实现捞取到事件后的本地业务执行或 MQ 投递。</p>
 * <p>注意：JPA 实体与仓库位于本模块包
 * {@code com.yineng.bpe.distributedsolutions.outbox}，若业务应用的主包不包含该包，
 * 需在启动类上追加 {@code @EntityScan} 与 {@code @EnableJpaRepositories} 指定本包。</p>
 */
@AutoConfiguration
@EnableConfigurationProperties(OutboxRelayProperties.class)
public class OutboxAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(OutboxPublisher.class)
    public OutboxPublisher outboxPublisher() {
        return new LoggingOutboxPublisher();
    }

    @Bean
    @ConditionalOnMissingBean
    public TransactionTemplate outboxTransactionTemplate(PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }

    @Bean
    @ConditionalOnMissingBean
    public OutboxRelay outboxRelay(OutboxMessageRepository repository,
                                   OutboxPublisher publisher,
                                   OutboxRelayProperties properties,
                                   TransactionTemplate transactionTemplate) {
        return new OutboxRelay(repository, publisher, properties, transactionTemplate);
    }

    /**
     * 仅当 outbox.relay.enabled=true 时开启定时调度；
     * 关闭后 OutboxRelay 仍可作为 Bean 注入，供测试或手动触发。
     */
    @Configuration(proxyBeanMethods = false)
    @EnableScheduling
    @ConditionalOnProperty(prefix = "outbox.relay", name = "enabled", havingValue = "true", matchIfMissing = true)
    static class OutboxSchedulingConfiguration {
    }
}
