package com.yineng.bpe.rabbit.confirm.message;

public record PointsMessage(String orderNo, Long userId, int points) {
}
