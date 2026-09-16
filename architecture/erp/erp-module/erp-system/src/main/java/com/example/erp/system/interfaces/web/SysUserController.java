package com.example.erp.system.interfaces.web;

import com.example.erp.system.api.SystemApi;
import com.example.erp.system.interfaces.web.dto.SysUserResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/system/users")
public class SysUserController {

    private final SystemApi queryService;

    public SysUserController(SystemApi queryService) {
        this.queryService = queryService;
    }

    @GetMapping("/{id}")
    public SysUserResponse get(@PathVariable String id) {
        var dto = queryService.findById(id);
        return new SysUserResponse(dto.id(), dto.name());
    }
}