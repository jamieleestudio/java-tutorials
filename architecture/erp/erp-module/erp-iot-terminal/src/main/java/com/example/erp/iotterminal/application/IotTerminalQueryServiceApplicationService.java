package com.example.erp.iotterminal.application;

import com.example.erp.iotterminal.api.IotTerminalQueryService;
import com.example.erp.iotterminal.api.dto.TerminalDto;
import com.example.erp.iotterminal.domain.model.Terminal;
import com.example.erp.iotterminal.domain.repository.TerminalRepository;
import com.example.erp.shared.EntityNotFoundException;
import com.example.erp.system.api.SystemQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class IotTerminalQueryServiceApplicationService implements IotTerminalQueryService {

    private final TerminalRepository repository;
    private final SystemQueryService systemQueryService;

    public IotTerminalQueryServiceApplicationService(TerminalRepository repository, SystemQueryService systemQueryService) {
        this.repository = repository;
        this.systemQueryService = systemQueryService;
    }

    @Override
    public TerminalDto findById(String id) {
        systemQueryService.currentTenantId();
        Terminal aggregate = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Terminal not found: " + id));
        return new TerminalDto(aggregate.id(), aggregate.name());
    }
}