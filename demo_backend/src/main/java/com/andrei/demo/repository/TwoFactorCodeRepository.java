package com.andrei.demo.repository;

import com.andrei.demo.model.TwoFactorCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TwoFactorCodeRepository
        extends JpaRepository<TwoFactorCode, UUID> {

    Optional<TwoFactorCode>
    findTopByEmailAndCodeAndUsedFalseOrderByExpiresAtDesc(
            String email,
            String code
    );
}