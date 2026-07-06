package com.andrei.demo.service;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.*;
import com.andrei.demo.repository.DepartamentRepository;
import com.andrei.demo.repository.PersonRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class PersonService {

    private final PersonRepository personRepository;
    private final DepartamentRepository departamentRepository;
    private final AuthorizationService authorizationService;
    private final PasswordEncoder passwordEncoder;

    public List<Person> getAll() {
        return personRepository.findAll();
    }

    public Person getById(UUID id) throws ValidationException {
        return personRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Person not found"));
    }

    public Person create(UUID loggedUserId, PersonCreateDTO dto) throws ValidationException {
        Person user = authorizationService.requireLoggedUser(loggedUserId);

        authorizationService.canCreatePerson(user);

        if (personRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new ValidationException("Email already exists");
        }

        Person person = new Person();
        person.setName(dto.getName());
        person.setEmail(dto.getEmail());
        person.setAge(dto.getAge());
        person.setRole(dto.getRole());

        if (dto.getDepartamentId() != null) {
            Departament dep = departamentRepository.findById(dto.getDepartamentId())
                    .orElseThrow(() -> new ValidationException("Department not found"));
            person.setDepartament(dep);
        }

        //parola default
        person.setPassword(passwordEncoder.encode("Password123!"));
        return personRepository.save(person);
    }

    public Person update(UUID loggedUserId, UUID id, PersonCreateDTO dto)
            throws ValidationException {

        Person user = authorizationService.requireLoggedUser(loggedUserId);

        Person target = personRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Person not found"));

        authorizationService.canEditPerson(user, target);
        authorizationService.validatePersonFieldChanges(
                user,
                target,
                dto.getDepartamentId(),
                dto.getRole()
        );

        if (dto.getEmail() != null) {
            Person existingWithEmail = personRepository.findByEmail(dto.getEmail()).orElse(null);

            if (existingWithEmail != null && !existingWithEmail.getId().equals(id)) {
                throw new ValidationException("Email already exists");
            }
        }

        target.setName(dto.getName());
        target.setEmail(dto.getEmail());
        target.setAge(dto.getAge());

        if (dto.getRole() != null) {
            target.setRole(dto.getRole());
        }

        if (dto.getDepartamentId() != null) {
            Departament dep = departamentRepository.findById(dto.getDepartamentId())
                    .orElseThrow(() -> new ValidationException("Department not found"));
            target.setDepartament(dep);
        }

        return personRepository.save(target);
    }

    public Person patch(UUID loggedUserId, UUID id, PersonPatchDTO dto)
            throws ValidationException {

        Person user = authorizationService.requireLoggedUser(loggedUserId);

        Person target = personRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Person not found"));

        authorizationService.canEditPerson(user, target);

        if (user.getRole() != PersonRole.TEAM_MANAGER) {
            if (dto.getRole() != null || dto.getDepartamentId() != null) {
                throw new ValidationException("Cannot change role or department");
            }
        }

        if (dto.getName() != null) {
            target.setName(dto.getName());
        }

        if (dto.getEmail() != null) {
            Person existingWithEmail = personRepository.findByEmail(dto.getEmail()).orElse(null);

            if (existingWithEmail != null && !existingWithEmail.getId().equals(id)) {
                throw new ValidationException("Email already exists");
            }

            target.setEmail(dto.getEmail());
        }

        if (dto.getAge() != null) {
            target.setAge(dto.getAge());
        }


        if (dto.getRole() != null) {
            target.setRole(dto.getRole());
        }

        if (dto.getDepartamentId() != null) {
            Departament dep = departamentRepository.findById(dto.getDepartamentId())
                    .orElseThrow(() -> new ValidationException("Department not found"));
            target.setDepartament(dep);
        }

        return personRepository.save(target);
    }

    public void delete(UUID loggedUserId, UUID id) throws ValidationException {
        Person user = authorizationService.requireLoggedUser(loggedUserId);

        authorizationService.canDeletePerson(user);

        if (!personRepository.existsById(id)) {
            throw new ValidationException("Person not found");
        }

        personRepository.deleteById(id);
    }
    public List<Person> getPeopleByDepartamentId(UUID departamentId) throws ValidationException {
        if (!departamentRepository.existsById(departamentId)) {
            throw new ValidationException("Department not found");
        }

        return personRepository.findByDepartamentId(departamentId);
    }
}