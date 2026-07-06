package com.andrei.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class LoginResponseDTO {
    private UUID id;
    private String name;
    private String email;
    private PersonRole role;
    private UUID departamentId;
    private String token;
    private String refreshToken;
}