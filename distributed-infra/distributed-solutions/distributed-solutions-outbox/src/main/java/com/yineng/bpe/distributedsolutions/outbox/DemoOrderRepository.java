package com.yineng.bpe.distributedsolutions.outbox;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 演示用订单表数据访问。
 */
public interface DemoOrderRepository extends JpaRepository<DemoOrder, Long> {
}
