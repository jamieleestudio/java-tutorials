package com.example.erp.system.application;

import com.example.erp.platform.kernel.TenantContext;
import com.example.erp.shared.EntityNotFoundException;
import com.example.erp.system.api.SystemQueryApi;
import com.example.erp.system.api.dto.SysUserDto;
import com.example.erp.system.domain.model.SysUser;
import com.example.erp.system.domain.repository.SysUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SystemApplicationService implements SystemQueryApi {

    private final SysUserRepository repository;

    public SystemApplicationService(SysUserRepository repository) {
        this.repository = repository;
    }

    @Override
    public String currentTenantId() {
        String tenantId = TenantContext.currentTenantId();
        return tenantId == null ? "default" : tenantId;
    }

    @Override
    public SysUserDto findById(String id) {
        SysUser user = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("SysUser not found: " + id));
        return new SysUserDto(user.id(), user.name());
    }
}