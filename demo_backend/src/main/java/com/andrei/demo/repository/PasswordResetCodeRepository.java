package com.andrei.demo.repository;

import com.andrei.demo.model.PasswordResetCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PasswordResetCodeRepository extends JpaRepository<PasswordResetCode, UUID> {

    Optional<PasswordResetCode> findTopByEmailAndCodeAndUsedFalseOrderByExpiresAtDesc(
            String email,
            String code
    );
}