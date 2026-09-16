package com.example.erp.iotterminal.application;

import com.example.erp.iotterminal.api.IotTerminalQueryApi;
import com.example.erp.iotterminal.api.dto.TerminalDto;
import com.example.erp.iotterminal.domain.model.Terminal;
import com.example.erp.iotterminal.domain.repository.TerminalRepository;
import com.example.erp.shared.EntityNotFoundException;
import com.example.erp.system.api.SystemQueryApi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class IotTerminalService implements IotTerminalQueryApi {

    private final TerminalRepository repository;
    private final SystemQueryApi systemQueryApi;

    public IotTerminalService(TerminalRepository repository, SystemQueryApi systemQueryApi) {
        this.repository = repository;
        this.systemQueryApi = systemQueryApi;
    }

    @Override
    public TerminalDto findById(String id) {
        systemQueryApi.currentTenantId();
        Terminal aggregate = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Terminal not found: " + id));
        return new TerminalDto(aggregate.id(), aggregate.name());
    }
}