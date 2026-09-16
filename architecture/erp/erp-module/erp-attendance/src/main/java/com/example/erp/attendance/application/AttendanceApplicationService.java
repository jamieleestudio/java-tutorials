package com.example.erp.attendance.application;

import com.example.erp.attendance.api.AttendanceTopics;
import com.example.erp.attendance.api.command.ClockInCommand;
import com.example.erp.attendance.api.dto.AttendanceRecordDto;
import com.example.erp.attendance.domain.model.AttendanceRecord;
import com.example.erp.attendance.domain.repository.AttendanceRecordRepository;
import com.example.erp.attendance.domain.service.FaceRecognitionPort;
import com.example.erp.grade.api.GradeApi;
import com.example.erp.platform.messaging.MessageEnvelope;
import com.example.erp.platform.messaging.MessagePublisher;
import com.example.erp.shared.BusinessRuleViolationException;
import com.example.erp.shared.EntityNotFoundException;
import com.example.erp.shared.IdGenerator;
import com.example.erp.system.api.SystemApi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AttendanceApplicationService {

    private final AttendanceRecordRepository repository;
    private final FaceRecognitionPort faceRecognitionPort;
    private final SystemApi systemApi;
    private final GradeApi gradeApi;
    private final MessagePublisher messagePublisher;

    public AttendanceApplicationService(AttendanceRecordRepository repository,
                                        FaceRecognitionPort faceRecognitionPort,
                                        SystemApi systemApi,
                                        GradeApi gradeApi,
                                        MessagePublisher messagePublisher) {
        this.repository = repository;
        this.faceRecognitionPort = faceRecognitionPort;
        this.systemApi = systemApi;
        this.gradeApi = gradeApi;
        this.messagePublisher = messagePublisher;
    }

    @Transactional
    public AttendanceRecordDto clockIn(ClockInCommand command) {
        systemApi.currentTenantId();
        if (command.faceToken() != null && !faceRecognitionPort.verify(command.studentId(), command.faceToken())) {
            throw new BusinessRuleViolationException("face verification failed for student " + command.studentId());
        }
        int gradeCount = gradeApi.findByStudentId(command.studentId()).size();
        AttendanceRecord record = AttendanceRecord.clockIn(IdGenerator.next(), command.studentId(), command.clockInTime(), null);
        AttendanceRecord saved = repository.save(record);
        messagePublisher.publish(MessageEnvelope.of(AttendanceTopics.CLOCKED, saved.id() + ":" + gradeCount));
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public AttendanceRecordDto findById(String id) {
        AttendanceRecord record = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("AttendanceRecord not found: " + id));
        return toDto(record);
    }

    @Transactional(readOnly = true)
    public List<AttendanceRecordDto> findByStudentId(String studentId) {
        return repository.findByStudentId(studentId).stream().map(this::toDto).toList();
    }

    private AttendanceRecordDto toDto(AttendanceRecord record) {
        return new AttendanceRecordDto(record.id(), record.studentId(), record.status().name(), record.clockInTime());
    }
}
