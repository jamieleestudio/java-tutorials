package com.example.erp.attendance.infrastructure.persistence;

import com.example.erp.attendance.domain.model.AttendanceRecord;
import com.example.erp.attendance.domain.model.AttendanceStatus;
import com.example.erp.attendance.domain.repository.AttendanceRecordRepository;
import com.example.erp.shared.IdGenerator;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class AttendanceRecordRepositoryImpl implements AttendanceRecordRepository {

    private final AttendanceRecordJpaRepository jpaRepository;

    public AttendanceRecordRepositoryImpl(AttendanceRecordJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public AttendanceRecord save(AttendanceRecord record) {
        AttendanceRecordEntity entity = new AttendanceRecordEntity();
        entity.setId(record.id() == null ? IdGenerator.next() : record.id());
        entity.setStudentId(record.studentId());
        entity.setStatus(record.status().name());
        entity.setClockInTime(record.clockInTime());
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<AttendanceRecord> findById(String id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<AttendanceRecord> findByStudentId(String studentId) {
        return jpaRepository.findByStudentId(studentId).stream().map(this::toDomain).toList();
    }

    private AttendanceRecord toDomain(AttendanceRecordEntity entity) {
        return new AttendanceRecord(entity.getId(), entity.getStudentId(),
                AttendanceStatus.valueOf(entity.getStatus()), entity.getClockInTime());
    }
}