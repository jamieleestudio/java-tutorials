package com.yineng.bpe.rabbit.transaction.message;

public record PointsMessage(String orderNo, Long userId, int points) {
}
