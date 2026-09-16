package com.example.erp.message.application;

import com.example.erp.message.api.MessageQueryApi;
import com.example.erp.message.api.dto.MessageDto;
import com.example.erp.message.domain.model.Message;
import com.example.erp.message.domain.repository.MessageRepository;
import com.example.erp.shared.EntityNotFoundException;
import com.example.erp.system.api.SystemQueryApi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MessageApplicationService implements MessageQueryApi {

    private final MessageRepository repository;
    private final SystemQueryApi systemQueryApi;

    public MessageApplicationService(MessageRepository repository, SystemQueryApi systemQueryApi) {
        this.repository = repository;
        this.systemQueryApi = systemQueryApi;
    }

    @Override
    public MessageDto findById(String id) {
        systemQueryApi.currentTenantId();
        Message aggregate = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Message not found: " + id));
        return new MessageDto(aggregate.id(), aggregate.name());
    }
}