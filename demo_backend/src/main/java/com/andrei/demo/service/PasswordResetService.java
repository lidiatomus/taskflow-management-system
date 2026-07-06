package com.andrei.demo.service;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.PasswordResetCode;
import com.andrei.demo.model.PasswordResetConfirmDTO;
import com.andrei.demo.model.PasswordResetRequestDTO;
import com.andrei.demo.model.Person;
import com.andrei.demo.repository.PasswordResetCodeRepository;
import com.andrei.demo.repository.PersonRepository;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import java.time.LocalDateTime;
import java.util.Random;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final PersonRepository personRepository;
    private final PasswordResetCodeRepository passwordResetCodeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;


    @Value("${spring.mail.username}")
    private String mailFrom;

    public void requestReset(PasswordResetRequestDTO dto) throws ValidationException {
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new ValidationException("Passwords do not match");
        }

        Person person = personRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new ValidationException("User with email " + dto.getEmail() + " not found"));

        String code = generateCode();

        PasswordResetCode resetCode = new PasswordResetCode();
        resetCode.setEmail(person.getEmail());
        resetCode.setCode(code);
        resetCode.setNewPassword(passwordEncoder.encode(dto.getNewPassword()));
        resetCode.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        resetCode.setUsed(false);

        passwordResetCodeRepository.save(resetCode);

        sendResetCodeEmail(person.getEmail(), code);
    }

    public void confirmReset(PasswordResetConfirmDTO dto) throws ValidationException {
        PasswordResetCode resetCode = passwordResetCodeRepository
                .findTopByEmailAndCodeAndUsedFalseOrderByExpiresAtDesc(dto.getEmail(), dto.getCode())
                .orElseThrow(() -> new ValidationException("Invalid reset code"));

        if (resetCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Reset code expired");
        }

        Person person = personRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new ValidationException("User with email " + dto.getEmail() + " not found"));

        person.setPassword(resetCode.getNewPassword());
        personRepository.save(person);

        resetCode.setUsed(true);
        passwordResetCodeRepository.save(resetCode);

        sendConfirmationEmail(person.getEmail());
    }

    private String generateCode() {
        return String.valueOf(100000 + new Random().nextInt(900000));
    }

    private void sendResetCodeEmail(String email, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(email);
        message.setSubject("Password reset code");
        message.setText("Your password reset code is: " + code + "\nThis code expires in 10 minutes.");

        System.out.println("Reset code for " + email + ": " + code);
        mailSender.send(message);
    }

    private void sendConfirmationEmail(String email) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(email);
        message.setSubject("Password changed");
        message.setText("Your password was recently updated. If this was not you, contact support immediately.");

        mailSender.send(message);
    }
}