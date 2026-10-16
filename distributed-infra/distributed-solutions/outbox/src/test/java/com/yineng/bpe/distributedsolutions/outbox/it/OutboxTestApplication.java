package com.yineng.bpe.distributedsolutions.outbox.it;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * 测试专用启动类：位于独立包，只注册测试上下文，
 * 通过 @EntityScan / @EnableJpaRepositories 显式挂载 outbox 模块的实体与仓库。
 */
@SpringBootApplication
@EntityScan("com.yineng.bpe.distributedsolutions.outbox")
@EnableJpaRepositories("com.yineng.bpe.distributedsolutions.outbox")
public class OutboxTestApplication {
}
