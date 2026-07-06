package com.andrei.demo.repository;

import com.andrei.demo.model.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, UUID> {

    Optional<LoginAttempt> findByEmail(String email);
}