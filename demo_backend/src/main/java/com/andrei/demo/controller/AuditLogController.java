package com.andrei.demo.controller;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.AuditLog;
import com.andrei.demo.model.Person;
import com.andrei.demo.model.PersonRole;
import com.andrei.demo.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin
@RequestMapping("/audit-log")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    public List<AuditLog> getAll(
            @AuthenticationPrincipal Person loggedUser
    ) throws ValidationException {

        if (loggedUser.getRole() != PersonRole.TEAM_MANAGER) {
            throw new ValidationException("Only TEAM_MANAGER can view audit log");
        }

        return auditLogService.getAll();
    }
}