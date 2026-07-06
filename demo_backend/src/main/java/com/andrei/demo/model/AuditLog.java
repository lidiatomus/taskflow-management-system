package com.andrei.demo.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private UUID actorId;

    private String actorEmail;

    private String actorRole;

    private String action;

    private String entityType;

    private UUID entityId;

    @Column(length = 1000)
    private String details;

    private LocalDateTime createdAt;
}