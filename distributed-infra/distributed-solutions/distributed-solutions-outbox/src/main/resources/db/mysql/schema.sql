-- Outbox 本地消息表（MySQL 8.0+，InnoDB，utf8mb4）
-- 幂等：CREATE TABLE IF NOT EXISTS，应用每次启动都会执行（spring.sql.init）
CREATE TABLE IF NOT EXISTS outbox_messages
(
    id             BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    aggregate_type VARCHAR(64)   NOT NULL COMMENT '聚合类型，如 ORDER',
    aggregate_id   VARCHAR(64)   NOT NULL COMMENT '聚合业务 ID，如订单号',
    event_type     VARCHAR(64)   NOT NULL COMMENT '事件类型，如 ORDER_CREATED',
    payload        VARCHAR(4096) NULL COMMENT '事件内容（JSON）',
    status         VARCHAR(16)   NOT NULL COMMENT '状态: PENDING / PUBLISHED / FAILED',
    retry_count    INT           NOT NULL DEFAULT 0 COMMENT '已失败重试次数',
    next_retry_at  DATETIME(3)   NOT NULL COMMENT '到期才可被定时任务捞取（退避）',
    created_at     DATETIME(3)   NOT NULL COMMENT '创建时间',
    published_at   DATETIME(3)   NULL COMMENT '消费成功时间',
    PRIMARY KEY (id),
    KEY idx_status_next_retry (status, next_retry_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='Outbox 本地消息表';

-- 演示用业务表：与 outbox 记录同事务写入
CREATE TABLE IF NOT EXISTS demo_orders
(
    id         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    order_no   VARCHAR(64) NOT NULL COMMENT '订单号',
    status     VARCHAR(32) NOT NULL COMMENT '订单状态',
    created_at DATETIME(3) NOT NULL COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='演示用业务表（订单）';
