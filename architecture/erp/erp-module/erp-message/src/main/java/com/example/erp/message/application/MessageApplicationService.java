package com.example.erp.message.application;

import com.example.erp.message.api.dto.MessageDto;
import com.example.erp.message.domain.model.Message;
import com.example.erp.message.domain.repository.MessageRepository;
import com.example.erp.shared.EntityNotFoundException;
import com.example.erp.system.api.SystemApi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MessageApplicationService {

    private final MessageRepository repository;
    private final SystemApi systemApi;

    public MessageApplicationService(MessageRepository repository, SystemApi systemApi) {
        this.repository = repository;
        this.systemApi = systemApi;
    }

    public MessageDto findById(String id) {
        systemApi.currentTenantId();
        Message aggregate = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Message not found: " + id));
        return new MessageDto(aggregate.id(), aggregate.name());
    }
}