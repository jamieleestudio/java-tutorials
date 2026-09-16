package com.example.erp.asset.interfaces.web;

import com.example.erp.asset.api.AssetApi;
import com.example.erp.asset.interfaces.web.dto.AssetResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/asset")
public class AssetController {

    private final AssetApi queryService;

    public AssetController(AssetApi queryService) {
        this.queryService = queryService;
    }

    @GetMapping("/{id}")
    public AssetResponse get(@PathVariable String id) {
        var dto = queryService.findById(id);
        return new AssetResponse(dto.id(), dto.name());
    }
}