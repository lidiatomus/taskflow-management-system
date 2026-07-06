package com.andrei.demo.controller;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.Person;
import com.andrei.demo.model.PersonCreateDTO;
import com.andrei.demo.model.PersonPatchDTO;
import com.andrei.demo.service.PersonService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@CrossOrigin
public class PersonController {

    private final PersonService personService;

    @GetMapping("/person")
    public List<Person> getAll() {
        return personService.getAll();
    }

    @GetMapping("/person/{id}")
    public Person getById(@PathVariable UUID id) throws ValidationException {
        return personService.getById(id);
    }

    @PostMapping("/person")
    public Person create(
            @AuthenticationPrincipal Person loggedUser,
            @Valid @RequestBody PersonCreateDTO dto
    ) throws ValidationException {
        return personService.create(loggedUser.getId(), dto);
    }

    @PutMapping("/person/{id}")
    public Person update(
            @AuthenticationPrincipal Person loggedUser,
            @PathVariable UUID id,
            @Valid @RequestBody PersonCreateDTO dto
    ) throws ValidationException {
        return personService.update(loggedUser.getId(), id, dto);
    }

    @PatchMapping("/person/{id}")
    public Person patch(
            @AuthenticationPrincipal Person loggedUser,
            @PathVariable UUID id,
            @Valid @RequestBody PersonPatchDTO dto
    ) throws ValidationException {
        return personService.patch(loggedUser.getId(), id, dto);
    }

    @DeleteMapping("/person/{id}")
    public void delete(
            @AuthenticationPrincipal Person loggedUser,
            @PathVariable UUID id
    ) throws ValidationException {
        personService.delete(loggedUser.getId(), id);
    }

    @GetMapping("/person/departament/{departamentId}")
    public List<Person> getByDepartamentId(@PathVariable UUID departamentId) throws ValidationException {
        return personService.getPeopleByDepartamentId(departamentId);
    }
}