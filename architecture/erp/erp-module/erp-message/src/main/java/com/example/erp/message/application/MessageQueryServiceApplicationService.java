package com.example.erp.message.application;

import com.example.erp.message.api.MessageQueryService;
import com.example.erp.message.api.dto.MessageDto;
import com.example.erp.message.domain.model.Message;
import com.example.erp.message.domain.repository.MessageRepository;
import com.example.erp.shared.EntityNotFoundException;
import com.example.erp.system.api.SystemQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MessageQueryServiceApplicationService implements MessageQueryService {

    private final MessageRepository repository;
    private final SystemQueryService systemQueryService;

    public MessageQueryServiceApplicationService(MessageRepository repository, SystemQueryService systemQueryService) {
        this.repository = repository;
        this.systemQueryService = systemQueryService;
    }

    @Override
    public MessageDto findById(String id) {
        systemQueryService.currentTenantId();
        Message aggregate = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Message not found: " + id));
        return new MessageDto(aggregate.id(), aggregate.name());
    }
}