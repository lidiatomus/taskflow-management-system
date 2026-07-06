package com.andrei.demo.model;

import lombok.Data;

@Data
public class LogoutRequestDTO {
    private String refreshToken;
}