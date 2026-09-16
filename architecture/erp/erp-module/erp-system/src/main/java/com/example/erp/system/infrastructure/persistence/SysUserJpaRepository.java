package com.example.erp.system.infrastructure.persistence;

import com.example.erp.platform.persistence.BaseJpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SysUserJpaRepository extends BaseJpaRepository<SysUserEntity, String> {
}