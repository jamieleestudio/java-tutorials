package com.example.erp.message.api;

import com.example.erp.message.api.dto.MessageDto;

public interface MessageQueryApi {

    MessageDto findById(String id);
}