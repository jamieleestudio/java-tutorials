package com.yineng.bpe.rabbit.txmessage.message;

public record PointsMessage(String orderNo, Long userId, int points) {
}
