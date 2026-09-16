package com.example.erp.message.interfaces.web;

import com.example.erp.message.api.MessageQueryApi;
import com.example.erp.message.interfaces.web.dto.MessageResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/message")
public class MessageController {

    private final MessageQueryApi queryService;

    public MessageController(MessageQueryApi queryService) {
        this.queryService = queryService;
    }

    @GetMapping("/{id}")
    public MessageResponse get(@PathVariable String id) {
        var dto = queryService.findById(id);
        return new MessageResponse(dto.id(), dto.name());
    }
}