package com.example.erp.message.infrastructure.persistence;

import com.example.erp.platform.persistence.BaseJpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageJpaRepository extends BaseJpaRepository<MessageEntity, String> {
}