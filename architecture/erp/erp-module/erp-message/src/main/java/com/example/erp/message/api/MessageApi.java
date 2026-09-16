package com.example.erp.message.api;

import com.example.erp.message.api.dto.MessageDto;

public interface MessageApi {

    MessageDto findById(String id);
}