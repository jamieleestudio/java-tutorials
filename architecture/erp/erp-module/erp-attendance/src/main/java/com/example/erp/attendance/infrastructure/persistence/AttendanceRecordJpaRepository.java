package com.example.erp.attendance.infrastructure.persistence;

import com.example.erp.platform.persistence.BaseJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttendanceRecordJpaRepository extends BaseJpaRepository<AttendanceRecordEntity, String> {

    List<AttendanceRecordEntity> findByStudentId(String studentId);
}