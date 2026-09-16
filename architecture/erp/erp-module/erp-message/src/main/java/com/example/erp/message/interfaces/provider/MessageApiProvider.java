package com.example.erp.message.interfaces.provider;

import com.example.erp.message.api.MessageApi;
import com.example.erp.message.api.dto.MessageDto;
import com.example.erp.message.application.MessageApplicationService;
import org.springframework.stereotype.Component;

@Component
public class MessageApiProvider implements MessageApi {

    private final MessageApplicationService applicationService;

    public MessageApiProvider(MessageApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @Override
    public MessageDto findById(String id) {
        return applicationService.findById(id);
    }
}