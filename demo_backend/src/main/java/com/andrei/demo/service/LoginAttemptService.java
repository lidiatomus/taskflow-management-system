package com.andrei.demo.service;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.LoginAttempt;
import com.andrei.demo.repository.LoginAttemptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final int BLOCK_MINUTES = 10;

    private final LoginAttemptRepository loginAttemptRepository;

    public void checkIfBlocked(String email) throws ValidationException {
        LoginAttempt attempt = loginAttemptRepository
                .findByEmail(email)
                .orElse(null);

        if (attempt == null || attempt.getBlockedUntil() == null) {
            return;
        }

        if (attempt.getBlockedUntil().isAfter(LocalDateTime.now())) {
            long secondsLeft = Duration
                    .between(LocalDateTime.now(), attempt.getBlockedUntil())
                    .toSeconds();

            throw new ValidationException(
                    "Too many failed login attempts. Try again in " + secondsLeft + " seconds."
            );
        }

        attempt.setFailedAttempts(0);
        attempt.setBlockedUntil(null);
        loginAttemptRepository.save(attempt);
    }

    public void loginFailed(String email) {
        LoginAttempt attempt = loginAttemptRepository
                .findByEmail(email)
                .orElseGet(() -> {
                    LoginAttempt newAttempt = new LoginAttempt();
                    newAttempt.setEmail(email);
                    return newAttempt;
                });

        int failedAttempts = attempt.getFailedAttempts() + 1;

        attempt.setFailedAttempts(failedAttempts);

        if (failedAttempts >= MAX_ATTEMPTS) {
            attempt.setBlockedUntil(
                    LocalDateTime.now().plusMinutes(BLOCK_MINUTES)
            );
        }

        loginAttemptRepository.save(attempt);
    }

    public void loginSucceeded(String email) {
        LoginAttempt attempt = loginAttemptRepository
                .findByEmail(email)
                .orElse(null);

        if (attempt == null) {
            return;
        }

        attempt.setFailedAttempts(0);
        attempt.setBlockedUntil(null);
        loginAttemptRepository.save(attempt);
    }
}