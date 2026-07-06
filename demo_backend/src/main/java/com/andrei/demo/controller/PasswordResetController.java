package com.andrei.demo.controller;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.PasswordResetConfirmDTO;
import com.andrei.demo.model.PasswordResetRequestDTO;
import com.andrei.demo.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@AllArgsConstructor
@CrossOrigin
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @PostMapping("/password-reset/request")
    public Map<String, String> requestReset(
            @Valid @RequestBody PasswordResetRequestDTO dto
    ) throws ValidationException {
        passwordResetService.requestReset(dto);
        return Map.of("message", "Reset code sent");
    }

    @PostMapping("/password-reset/confirm")
    public Map<String, String> confirmReset(
            @Valid @RequestBody PasswordResetConfirmDTO dto
    ) throws ValidationException {
        passwordResetService.confirmReset(dto);
        return Map.of("message", "Password changed successfully");
    }
}