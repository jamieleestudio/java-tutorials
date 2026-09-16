package com.example.erp.message.domain.repository;

import com.example.erp.message.domain.model.Message;

import java.util.Optional;

public interface MessageRepository {

    Message save(Message aggregate);

    Optional<Message> findById(String id);
}