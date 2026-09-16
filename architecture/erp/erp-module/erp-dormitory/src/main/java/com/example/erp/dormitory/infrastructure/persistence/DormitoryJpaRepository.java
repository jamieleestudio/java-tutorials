package com.example.erp.dormitory.infrastructure.persistence;

import com.example.erp.platform.persistence.BaseJpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DormitoryJpaRepository extends BaseJpaRepository<DormitoryEntity, String> {
}