package com.example.erp.attendance.interfaces.mq;

import com.example.erp.attendance.api.AttendanceTopics;
import com.example.erp.platform.messaging.MessageConsumer;
import com.example.erp.platform.messaging.MessageEnvelope;
import org.springframework.stereotype.Component;

@Component
public class AttendanceClockConsumer implements MessageConsumer {

    private String lastPayload;

    @Override
    public String topic() {
        return AttendanceTopics.CLOCKED;
    }

    @Override
    public void onMessage(MessageEnvelope envelope) {
        this.lastPayload = envelope.payload();
    }

    public String lastPayload() {
        return lastPayload;
    }
}