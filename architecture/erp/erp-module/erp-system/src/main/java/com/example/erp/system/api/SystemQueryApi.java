package com.example.erp.system.api;

import com.example.erp.system.api.dto.SysUserDto;

public interface SystemQueryApi {

    String currentTenantId();

    SysUserDto findById(String id);
}