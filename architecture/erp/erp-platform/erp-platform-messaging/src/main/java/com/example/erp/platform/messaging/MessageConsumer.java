package com.example.erp.platform.messaging;

public interface MessageConsumer {

    String topic();

    void onMessage(MessageEnvelope envelope);
}