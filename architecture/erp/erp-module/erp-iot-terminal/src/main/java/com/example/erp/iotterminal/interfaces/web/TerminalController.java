package com.example.erp.iotterminal.interfaces.web;

import com.example.erp.iotterminal.application.IotTerminalApplicationService;
import com.example.erp.iotterminal.interfaces.web.dto.TerminalResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/iot-terminal")
public class TerminalController {

    private final IotTerminalApplicationService applicationService;

    public TerminalController(IotTerminalApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping("/{id}")
    public TerminalResponse get(@PathVariable String id) {
        var dto = applicationService.findById(id);
        return new TerminalResponse(dto.id(), dto.name());
    }
}