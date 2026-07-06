package com.andrei.demo.controller;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.Departament;
import com.andrei.demo.model.DepartamentCreateDTO;
import com.andrei.demo.model.DepartamentPatchDTO;
import com.andrei.demo.model.Person;
import com.andrei.demo.service.DepartamentService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@CrossOrigin
public class DepartamentController {

    private final DepartamentService departamentService;

    @GetMapping("/departament")
    public List<Departament> getDepartaments(
            @AuthenticationPrincipal Person loggedUser
    ) throws ValidationException {
        return departamentService.getDepartaments(loggedUser.getId());
    }

    @GetMapping("/departament/{uuid}")
    public Departament getDepartamentById(
            @AuthenticationPrincipal Person loggedUser,
            @PathVariable UUID uuid
    ) throws ValidationException {
        return departamentService.getDepartamentById(loggedUser.getId(), uuid);
    }

    @PostMapping("/departament")
    public Departament addDepartament(
            @AuthenticationPrincipal Person loggedUser,
            @Valid @RequestBody DepartamentCreateDTO departamentDTO
    ) throws ValidationException {
        return departamentService.addDepartament(loggedUser.getId(), departamentDTO);
    }

    @PutMapping("/departament/{uuid}")
    public Departament updateDepartament(
            @AuthenticationPrincipal Person loggedUser,
            @PathVariable UUID uuid,
            @Valid @RequestBody DepartamentCreateDTO departamentDTO
    ) throws ValidationException {
        return departamentService.updateDepartment(loggedUser.getId(), uuid, departamentDTO);
    }

    @DeleteMapping("/departament/{uuid}")
    public void deleteDepartament(
            @AuthenticationPrincipal Person loggedUser,
            @PathVariable UUID uuid
    ) throws ValidationException {
        departamentService.deleteDepartament(loggedUser.getId(), uuid);
    }

    @PatchMapping("/departament/{uuid}")
    public Departament patchDepartament(
            @AuthenticationPrincipal Person loggedUser,
            @PathVariable UUID uuid,
            @Valid @RequestBody DepartamentPatchDTO departamentPatchDTO
    ) throws ValidationException {
        return departamentService.patchDepartament(loggedUser.getId(), uuid, departamentPatchDTO);
    }
}