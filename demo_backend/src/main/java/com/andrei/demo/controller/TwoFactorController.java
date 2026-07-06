package com.andrei.demo.controller;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.LoginResponseDTO;
import com.andrei.demo.model.Verify2FADTO;
import com.andrei.demo.service.TwoFactorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@CrossOrigin
public class TwoFactorController {

    private final TwoFactorService twoFactorService;

    @PostMapping("/verify-2fa")
    public LoginResponseDTO verify(
            @RequestBody Verify2FADTO dto
    ) throws ValidationException {
        return twoFactorService.verify(dto);
    }
}