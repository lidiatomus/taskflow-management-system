package com.andrei.demo.service;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.Person;
import com.andrei.demo.model.PersonRole;
import com.andrei.demo.repository.PersonRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import com.andrei.demo.model.Task;
import java.util.UUID;

@Service
@AllArgsConstructor
public class AuthorizationService {

    private final PersonRepository personRepository;

    public Person requireLoggedUser(UUID userId) throws ValidationException {
        return personRepository.findById(userId)
                .orElseThrow(() -> new ValidationException("Logged user not found"));
    }


    public void canCreatePerson(Person user) throws ValidationException {
        if (user.getRole() != PersonRole.TEAM_MANAGER) {
            throw new ValidationException("Only TEAM_MANAGER can create persons");
        }
    }


    public void canDeletePerson(Person user) throws ValidationException {
        if (user.getRole() != PersonRole.TEAM_MANAGER) {
            throw new ValidationException("Only TEAM_MANAGER can delete persons");
        }
    }


    public void canEditPerson(Person user, Person target) throws ValidationException {
        if (user.getRole() == PersonRole.TEAM_MANAGER) {
            return;
        }

        if (user.getRole() == PersonRole.DEPARTMENT_HEAD) {
            if (target.getDepartament() == null ||
                    !target.getDepartament().getId().equals(user.getDepartament().getId())) {
                throw new ValidationException("Cannot edit person outside your department");
            }
            return;
        }

        if (user.getRole() == PersonRole.MEMBER) {
            if (!user.getId().equals(target.getId())) {
                throw new ValidationException("You can edit only your own profile");
            }
            return;
        }

        throw new ValidationException("Unauthorized");
    }


    public void validatePersonFieldChanges(
            Person user,
            Person target,
            UUID newDepartamentId,
            PersonRole newRole
    ) throws ValidationException {

        if (user.getRole() == PersonRole.TEAM_MANAGER) {
            return;
        }

        if (user.getRole() == PersonRole.DEPARTMENT_HEAD) {
            if (newRole != null) {
                throw new ValidationException("Cannot change role");
            }
            if (newDepartamentId != null) {
                throw new ValidationException("Cannot change department");
            }
            return;
        }

        if (user.getRole() == PersonRole.MEMBER) {
            if (newRole != null || newDepartamentId != null) {
                throw new ValidationException("Cannot change role or department");
            }
        }
    }

    public void canCreateTask(Person user, java.util.List<Person> assignedPersons)
            throws ValidationException {

        if (user.getRole() == PersonRole.TEAM_MANAGER) {
            return;
        }

        if (user.getRole() == PersonRole.DEPARTMENT_HEAD) {
            UUID depId = user.getDepartament().getId();

            boolean allSameDepartment = assignedPersons.stream()
                    .allMatch(p ->
                            p.getDepartament() != null &&
                                    p.getDepartament().getId().equals(depId)
                    );

            if (!allSameDepartment) {
                throw new ValidationException("Department head can assign only persons from their department");
            }

            return;
        }

        throw new ValidationException("Only manager or department head can create tasks");
    }


    public void canEditTask(Person user, Task task) throws ValidationException {

        if (user.getRole() == PersonRole.TEAM_MANAGER) {
            return;
        }

        if (user.getRole() == PersonRole.DEPARTMENT_HEAD) {
            UUID depId = user.getDepartament().getId();

            boolean belongs = task.getAssignedPersons().stream()
                    .anyMatch(p ->
                            p.getDepartament() != null &&
                                    p.getDepartament().getId().equals(depId)
                    );

            if (!belongs) {
                throw new ValidationException("Cannot edit task outside your department");
            }

            return;
        }

        if (user.getRole() == PersonRole.MEMBER) {
            boolean assigned = task.getAssignedPersons().stream()
                    .anyMatch(p -> p.getId().equals(user.getId()));

            if (!assigned) {
                throw new ValidationException("You can edit only your tasks");
            }

            return;
        }

        throw new ValidationException("Unauthorized");
    }


    public void canDeleteTask(Person user) throws ValidationException {
        if (user.getRole() != PersonRole.TEAM_MANAGER) {
            throw new ValidationException("Only TEAM_MANAGER can delete tasks");
        }
    }


    public void ensureMemberCanOnlyChangeTaskStatus(
            Person user,
            Task task,
            boolean changesStatus,
            boolean changesOtherFields
    ) throws ValidationException {

        if (user.getRole() != PersonRole.MEMBER) {
            return;
        }

        boolean assigned = task.getAssignedPersons().stream()
                .anyMatch(p -> p.getId().equals(user.getId()));

        if (!assigned) {
            throw new ValidationException("You can modify only your tasks");
        }

        if (changesOtherFields) {
            throw new ValidationException("MEMBER can only change task status");
        }

        if (!changesStatus) {
            throw new ValidationException("You must change at least the status");
        }
    }


    public void canViewDepartament(Person user) throws ValidationException {
        if (user == null) {
            throw new ValidationException("Unauthorized");
        }
    }

    public void canCreateDepartament(Person user) throws ValidationException {
        if (user.getRole() != PersonRole.TEAM_MANAGER) {
            throw new ValidationException("Only TEAM_MANAGER can create departments");
        }
    }

    public void canEditDepartament(Person user, com.andrei.demo.model.Departament departament)
            throws ValidationException {

        if (user.getRole() == PersonRole.TEAM_MANAGER) {
            return;
        }

        if (user.getRole() == PersonRole.DEPARTMENT_HEAD) {
            if (user.getDepartament() == null ||
                    !user.getDepartament().getId().equals(departament.getId())) {
                throw new ValidationException("DEPARTMENT_HEAD can edit only their own department");
            }
            return;
        }

        throw new ValidationException("You are not allowed to edit this department");
    }

    public void canDeleteDepartament(Person user) throws ValidationException {
        if (user.getRole() != PersonRole.TEAM_MANAGER) {
            throw new ValidationException("Only TEAM_MANAGER can delete departments");
        }
    }


}