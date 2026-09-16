package com.example.erp.system.infrastructure.persistence;

import com.example.erp.shared.IdGenerator;
import com.example.erp.system.domain.model.SysUser;
import com.example.erp.system.domain.repository.SysUserRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class SysUserRepositoryImpl implements SysUserRepository {

    private final SysUserJpaRepository jpaRepository;

    public SysUserRepositoryImpl(SysUserJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public SysUser save(SysUser aggregate) {
        SysUserEntity entity = new SysUserEntity();
        entity.setId(aggregate.id() == null ? IdGenerator.next() : aggregate.id());
        entity.setName(aggregate.name());
        SysUserEntity saved = jpaRepository.save(entity);
        return new SysUser(saved.getId(), saved.getName());
    }

    @Override
    public Optional<SysUser> findById(String id) {
        return jpaRepository.findById(id).map(e -> new SysUser(e.getId(), e.getName()));
    }
}