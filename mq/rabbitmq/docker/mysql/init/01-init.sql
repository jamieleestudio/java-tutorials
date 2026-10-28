CREATE DATABASE IF NOT EXISTS db_confirm DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS db_transaction DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS db_reliable DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS db_txmsg DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 方案3：消息落库 + 补偿重发
USE db_reliable;

CREATE TABLE IF NOT EXISTS t_payment_order (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no    VARCHAR(64)  NOT NULL COMMENT '订单号，业务幂等键',
    user_id     BIGINT       NOT NULL,
    amount      DECIMAL(10, 2) NOT NULL,
    status      VARCHAR(16)  NOT NULL DEFAULT 'PAID' COMMENT 'PAID-已支付',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_order_no (order_no)
) COMMENT '支付订单表';

CREATE TABLE IF NOT EXISTS t_local_message (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    message_id      VARCHAR(64)  NOT NULL COMMENT '消息唯一ID',
    biz_key         VARCHAR(64)  NOT NULL COMMENT '业务键，如订单号',
    exchange        VARCHAR(64)  NOT NULL,
    routing_key     VARCHAR(64)  NOT NULL,
    payload         TEXT         NOT NULL COMMENT '消息体JSON',
    status          TINYINT      NOT NULL DEFAULT 0 COMMENT '0-待发送 1-已发送',
    retry_count     INT          NOT NULL DEFAULT 0,
    next_retry_time DATETIME     NOT NULL COMMENT '下次补偿重发时间',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_message_id (message_id),
    KEY idx_status_retry (status, next_retry_time)
) COMMENT '本地消息表';

CREATE TABLE IF NOT EXISTS t_points_record (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no   VARCHAR(64) NOT NULL COMMENT '消费端幂等键',
    user_id    BIGINT      NOT NULL,
    points     INT         NOT NULL,
    created_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_order_no (order_no)
) COMMENT '积分发放记录';

-- 方案4：仿事务消息（半消息）
USE db_txmsg;

CREATE TABLE IF NOT EXISTS t_payment_order (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no    VARCHAR(64)  NOT NULL COMMENT '订单号，业务幂等键',
    user_id     BIGINT       NOT NULL,
    amount      DECIMAL(10, 2) NOT NULL,
    status      VARCHAR(16)  NOT NULL DEFAULT 'PAID' COMMENT 'PAID-已支付',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_order_no (order_no)
) COMMENT '支付订单表';

CREATE TABLE IF NOT EXISTS t_tx_message (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    message_id  VARCHAR(64)  NOT NULL COMMENT '消息唯一ID',
    biz_key     VARCHAR(64)  NOT NULL COMMENT '业务键，如订单号',
    exchange    VARCHAR(64)  NOT NULL,
    routing_key VARCHAR(64)  NOT NULL,
    payload     TEXT         NOT NULL COMMENT '消息体JSON',
    status      TINYINT      NOT NULL DEFAULT 0 COMMENT '0-半消息(预备) 1-已提交(待发送) 2-已回滚',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_message_id (message_id),
    KEY idx_status (status)
) COMMENT '事务消息表（仿RocketMQ半消息）';

CREATE TABLE IF NOT EXISTS t_points_record (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no   VARCHAR(64) NOT NULL COMMENT '消费端幂等键',
    user_id    BIGINT      NOT NULL,
    points     INT         NOT NULL,
    created_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_order_no (order_no)
) COMMENT '积分发放记录';
