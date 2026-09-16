package com.example.erp.system.interfaces.provider;

import com.example.erp.system.api.SystemApi;
import com.example.erp.system.api.dto.SysUserDto;
import com.example.erp.system.application.SystemApplicationService;
import org.springframework.stereotype.Component;

@Component
public class SystemApiProvider implements SystemApi {

    private final SystemApplicationService applicationService;

    public SystemApiProvider(SystemApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @Override
    public String currentTenantId() {
        return applicationService.currentTenantId();
    }

    @Override
    public SysUserDto findById(String id) {
        return applicationService.findById(id);
    }
}