package com.example.erp.attendance.application;

import com.example.erp.attendance.api.AttendanceClockService;
import com.example.erp.attendance.api.AttendanceQueryService;
import com.example.erp.attendance.api.AttendanceTopics;
import com.example.erp.attendance.api.command.ClockInCommand;
import com.example.erp.attendance.api.dto.AttendanceRecordDto;
import com.example.erp.attendance.domain.model.AttendanceRecord;
import com.example.erp.attendance.domain.repository.AttendanceRecordRepository;
import com.example.erp.attendance.domain.service.FaceRecognitionPort;
import com.example.erp.grade.api.GradeQueryService;
import com.example.erp.platform.messaging.MessageEnvelope;
import com.example.erp.platform.messaging.MessagePublisher;
import com.example.erp.shared.BusinessRuleViolationException;
import com.example.erp.shared.EntityNotFoundException;
import com.example.erp.shared.IdGenerator;
import com.example.erp.system.api.SystemQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AttendanceClockApplicationService implements AttendanceQueryService, AttendanceClockService {

    private final AttendanceRecordRepository repository;
    private final FaceRecognitionPort faceRecognitionPort;
    private final SystemQueryService systemQueryService;
    private final GradeQueryService gradeQueryService;
    private final MessagePublisher messagePublisher;

    public AttendanceClockApplicationService(AttendanceRecordRepository repository,
                                             FaceRecognitionPort faceRecognitionPort,
                                             SystemQueryService systemQueryService,
                                             GradeQueryService gradeQueryService,
                                             MessagePublisher messagePublisher) {
        this.repository = repository;
        this.faceRecognitionPort = faceRecognitionPort;
        this.systemQueryService = systemQueryService;
        this.gradeQueryService = gradeQueryService;
        this.messagePublisher = messagePublisher;
    }

    @Override
    @Transactional
    public AttendanceRecordDto clockIn(ClockInCommand command) {
        systemQueryService.currentTenantId();
        if (command.faceToken() != null && !faceRecognitionPort.verify(command.studentId(), command.faceToken())) {
            throw new BusinessRuleViolationException("face verification failed for student " + command.studentId());
        }
        int gradeCount = gradeQueryService.findByStudentId(command.studentId()).size();
        AttendanceRecord record = AttendanceRecord.clockIn(IdGenerator.next(), command.studentId(), command.clockInTime(), null);
        AttendanceRecord saved = repository.save(record);
        messagePublisher.publish(MessageEnvelope.of(AttendanceTopics.CLOCKED, saved.id() + ":" + gradeCount));
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceRecordDto findById(String id) {
        AttendanceRecord record = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("AttendanceRecord not found: " + id));
        return toDto(record);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceRecordDto> findByStudentId(String studentId) {
        return repository.findByStudentId(studentId).stream().map(this::toDto).toList();
    }

    private AttendanceRecordDto toDto(AttendanceRecord record) {
        return new AttendanceRecordDto(record.id(), record.studentId(), record.status().name(), record.clockInTime());
    }
}