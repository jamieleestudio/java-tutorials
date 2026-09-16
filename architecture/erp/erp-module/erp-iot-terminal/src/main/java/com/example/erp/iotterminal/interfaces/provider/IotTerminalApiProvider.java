package com.example.erp.iotterminal.interfaces.provider;

import com.example.erp.iotterminal.api.IotTerminalApi;
import com.example.erp.iotterminal.api.dto.TerminalDto;
import com.example.erp.iotterminal.application.IotTerminalApplicationService;
import org.springframework.stereotype.Component;

@Component
public class IotTerminalApiProvider implements IotTerminalApi {

    private final IotTerminalApplicationService applicationService;

    public IotTerminalApiProvider(IotTerminalApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @Override
    public TerminalDto findById(String id) {
        return applicationService.findById(id);
    }
}