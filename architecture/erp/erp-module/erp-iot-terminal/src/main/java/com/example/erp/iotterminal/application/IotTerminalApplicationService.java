package com.example.erp.iotterminal.application;

import com.example.erp.iotterminal.api.dto.TerminalDto;
import com.example.erp.iotterminal.domain.model.Terminal;
import com.example.erp.iotterminal.domain.repository.TerminalRepository;
import com.example.erp.shared.EntityNotFoundException;
import com.example.erp.system.api.SystemApi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class IotTerminalApplicationService {

    private final TerminalRepository repository;
    private final SystemApi systemApi;

    public IotTerminalApplicationService(TerminalRepository repository, SystemApi systemApi) {
        this.repository = repository;
        this.systemApi = systemApi;
    }

    public TerminalDto findById(String id) {
        systemApi.currentTenantId();
        Terminal aggregate = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Terminal not found: " + id));
        return new TerminalDto(aggregate.id(), aggregate.name());
    }
}