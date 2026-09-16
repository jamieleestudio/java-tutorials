package com.example.erp.grade.infrastructure.persistence;

import com.example.erp.platform.persistence.BaseJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GradeJpaRepository extends BaseJpaRepository<GradeEntity, String> {

    List<GradeEntity> findByStudentId(String studentId);
}