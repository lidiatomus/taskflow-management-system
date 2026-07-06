package com.andrei.demo.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
public class TwoFactorCode {

    @Id
    @GeneratedValue
    private UUID id;

    private String email;

    private String code;

    private LocalDateTime expiresAt;

    private boolean used;
}