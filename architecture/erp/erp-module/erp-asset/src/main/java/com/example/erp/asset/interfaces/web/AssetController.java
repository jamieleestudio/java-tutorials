package com.example.erp.asset.interfaces.web;

import com.example.erp.asset.application.AssetApplicationService;
import com.example.erp.asset.interfaces.web.dto.AssetResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/asset")
public class AssetController {

    private final AssetApplicationService applicationService;

    public AssetController(AssetApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping("/{id}")
    public AssetResponse get(@PathVariable String id) {
        var dto = applicationService.findById(id);
        return new AssetResponse(dto.id(), dto.name());
    }
}