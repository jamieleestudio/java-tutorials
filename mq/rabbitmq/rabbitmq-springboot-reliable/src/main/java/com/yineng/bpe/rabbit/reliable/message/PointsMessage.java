package com.yineng.bpe.rabbit.reliable.message;

public record PointsMessage(String orderNo, Long userId, int points) {
}
