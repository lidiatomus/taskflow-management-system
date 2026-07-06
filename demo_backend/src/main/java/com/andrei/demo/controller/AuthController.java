package com.andrei.demo.controller;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.*;
import com.andrei.demo.service.AuthService;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@CrossOrigin
public class AuthController {

    private final AuthService authService;

    @PostConstruct
    public void init() {
        System.out.println(">>> AuthController loaded");
    }

    @GetMapping("/login-test")
    public String loginTest() {
        return "auth works";
    }

    @PostMapping("/login")
    public LoginStepResponseDTO login(
            @RequestBody LoginRequestDTO dto
    ) throws ValidationException {

        return authService.login(dto);
    }
    @PostMapping("/auth/refresh")
    public LoginResponseDTO refresh(
            @RequestBody RefreshTokenRequestDTO dto
    ) throws ValidationException {

        return authService.refreshToken(
                dto.getRefreshToken()
        );
    }

    @PostMapping("/auth/logout")
    public void logout(
            @RequestBody LogoutRequestDTO dto
    ) throws ValidationException {

        authService.logout(
                dto.getRefreshToken()
        );
    }
}