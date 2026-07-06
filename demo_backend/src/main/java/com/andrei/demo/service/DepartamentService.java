package com.andrei.demo.service;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.Departament;
import com.andrei.demo.model.DepartamentCreateDTO;
import com.andrei.demo.model.DepartamentPatchDTO;
import com.andrei.demo.model.Person;
import com.andrei.demo.repository.DepartamentRepository;
import com.andrei.demo.repository.PersonRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class DepartamentService {
    private final DepartamentRepository departamentRepository;
    private final PersonRepository personRepository;
    private final AuthorizationService authorizationService;

    public List<Departament> getDepartaments(UUID loggedUserId) throws ValidationException {
        Person loggedUser = authorizationService.requireLoggedUser(loggedUserId);
        authorizationService.canViewDepartament(loggedUser);

        return departamentRepository.findAll();
    }

    public Departament getDepartamentById(UUID loggedUserId, UUID uuid) throws ValidationException {
        Person loggedUser = authorizationService.requireLoggedUser(loggedUserId);
        authorizationService.canViewDepartament(loggedUser);

        return departamentRepository.findById(uuid).orElseThrow(
                () -> new ValidationException("Departament with id " + uuid + " not found")
        );
    }

    public Departament addDepartament(UUID loggedUserId, DepartamentCreateDTO departamentDTO)
            throws ValidationException {

        Person loggedUser = authorizationService.requireLoggedUser(loggedUserId);
        authorizationService.canCreateDepartament(loggedUser);

        if (departamentRepository.findByName(departamentDTO.getName()).isPresent()) {
            throw new ValidationException("Departament name already exists");
        }

        Departament departament = new Departament();
        departament.setName(departamentDTO.getName());
        departament.setDescription(departamentDTO.getDescription());

        return departamentRepository.save(departament);
    }

    public void deleteDepartament(UUID loggedUserId, UUID uuid) throws ValidationException {
        Person loggedUser = authorizationService.requireLoggedUser(loggedUserId);
        authorizationService.canDeleteDepartament(loggedUser);

        Departament departament = departamentRepository.findById(uuid).orElseThrow(
                () -> new ValidationException("Departament with id " + uuid + " not found")
        );

        if (departament.getMembers() != null) {
            for (Person person : departament.getMembers()) {
                person.setDepartament(null);
            }
            personRepository.saveAll(departament.getMembers());
        }

        departamentRepository.delete(departament);
    }

    public Departament updateDepartment(UUID loggedUserId, UUID uuid, DepartamentCreateDTO departamentDTO)
            throws ValidationException {

        Person loggedUser = authorizationService.requireLoggedUser(loggedUserId);

        Departament existingDepartment = departamentRepository.findById(uuid).orElseThrow(
                () -> new ValidationException("Department with id " + uuid + " not found")
        );

        authorizationService.canEditDepartament(loggedUser, existingDepartment);

        if (departamentRepository.findByName(departamentDTO.getName()).isPresent() &&
                !existingDepartment.getName().equals(departamentDTO.getName())) {
            throw new ValidationException("Department name already exists");
        }

        existingDepartment.setName(departamentDTO.getName());
        existingDepartment.setDescription(departamentDTO.getDescription());

        return departamentRepository.save(existingDepartment);
    }

    public Departament patchDepartament(UUID loggedUserId, UUID uuid, DepartamentPatchDTO departamentPatchDTO)
            throws ValidationException {

        Person loggedUser = authorizationService.requireLoggedUser(loggedUserId);

        Departament existingDepartment = departamentRepository.findById(uuid).orElseThrow(
                () -> new ValidationException("Department with id " + uuid + " not found")
        );

        authorizationService.canEditDepartament(loggedUser, existingDepartment);

        if (departamentPatchDTO.getName() != null) {
            if (departamentRepository.findByName(departamentPatchDTO.getName()).isPresent() &&
                    !existingDepartment.getName().equals(departamentPatchDTO.getName())) {
                throw new ValidationException("Department name already exists");
            }
            existingDepartment.setName(departamentPatchDTO.getName());
        }

        if (departamentPatchDTO.getDescription() != null) {
            existingDepartment.setDescription(departamentPatchDTO.getDescription());
        }

        return departamentRepository.save(existingDepartment);
    }
}