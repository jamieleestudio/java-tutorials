package com.example.erp.system.domain.repository;

import com.example.erp.system.domain.model.SysUser;

import java.util.Optional;

public interface SysUserRepository {

    SysUser save(SysUser aggregate);

    Optional<SysUser> findById(String id);
}