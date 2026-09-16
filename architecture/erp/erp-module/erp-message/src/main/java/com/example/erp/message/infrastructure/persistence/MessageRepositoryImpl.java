package com.example.erp.message.infrastructure.persistence;

import com.example.erp.message.domain.model.Message;
import com.example.erp.message.domain.repository.MessageRepository;
import com.example.erp.shared.IdGenerator;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class MessageRepositoryImpl implements MessageRepository {

    private final MessageJpaRepository jpaRepository;

    public MessageRepositoryImpl(MessageJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Message save(Message aggregate) {
        MessageEntity entity = new MessageEntity();
        entity.setId(aggregate.id() == null ? IdGenerator.next() : aggregate.id());
        entity.setName(aggregate.name());
        MessageEntity saved = jpaRepository.save(entity);
        return new Message(saved.getId(), saved.getName());
    }

    @Override
    public Optional<Message> findById(String id) {
        return jpaRepository.findById(id).map(e -> new Message(e.getId(), e.getName()));
    }
}