package com.andrei.demo.service;

import com.andrei.demo.model.AuditLog;
import com.andrei.demo.model.Person;
import com.andrei.demo.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public void log(
            Person actor,
            String action,
            String entityType,
            UUID entityId,
            String details
    ) {
        AuditLog auditLog = new AuditLog();

        auditLog.setActorId(actor.getId());
        auditLog.setActorEmail(actor.getEmail());
        auditLog.setActorRole(actor.getRole().name());
        auditLog.setAction(action);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setDetails(details);
        auditLog.setCreatedAt(LocalDateTime.now());

        auditLogRepository.save(auditLog);
    }

    public java.util.List<AuditLog> getAll() {
        return auditLogRepository.findAllByOrderByCreatedAtDesc();
    }
}