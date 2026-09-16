package com.example.erp.message.interfaces.web;

import com.example.erp.message.application.MessageApplicationService;
import com.example.erp.message.interfaces.web.dto.MessageResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/message")
public class MessageController {

    private final MessageApplicationService applicationService;

    public MessageController(MessageApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping("/{id}")
    public MessageResponse get(@PathVariable String id) {
        var dto = applicationService.findById(id);
        return new MessageResponse(dto.id(), dto.name());
    }
}