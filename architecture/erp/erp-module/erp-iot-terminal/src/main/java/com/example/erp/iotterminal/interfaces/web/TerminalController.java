package com.example.erp.iotterminal.interfaces.web;

import com.example.erp.iotterminal.api.IotTerminalApi;
import com.example.erp.iotterminal.interfaces.web.dto.TerminalResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/iot-terminal")
public class TerminalController {

    private final IotTerminalApi queryService;

    public TerminalController(IotTerminalApi queryService) {
        this.queryService = queryService;
    }

    @GetMapping("/{id}")
    public TerminalResponse get(@PathVariable String id) {
        var dto = queryService.findById(id);
        return new TerminalResponse(dto.id(), dto.name());
    }
}