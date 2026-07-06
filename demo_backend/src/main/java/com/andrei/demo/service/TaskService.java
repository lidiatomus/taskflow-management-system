package com.andrei.demo.service;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.*;
import com.andrei.demo.repository.PersonRepository;
import com.andrei.demo.repository.TaskRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final PersonRepository personRepository;
    private final AuthorizationService authorizationService;

    public List<Task> getTasks() {
        return taskRepository.findAllWithPersons();
    }

    public Task getTaskById(UUID uuid) throws ValidationException {
        return taskRepository.findById(uuid).orElseThrow(
                () -> new ValidationException("Task with id " + uuid + " not found")
        );
    }

    public Task addTask(UUID loggedUserId, TaskCreateDTO taskDTO) throws ValidationException {
        Person loggedUser = authorizationService.requireLoggedUser(loggedUserId);

        List<Person> assignedPersons = new ArrayList<>();

        for (UUID personId : taskDTO.getPersonIds()) {
            Person person = personRepository.findById(personId).orElseThrow(
                    () -> new ValidationException("Person with id " + personId + " not found")
            );
            assignedPersons.add(person);
        }

        authorizationService.canCreateTask(loggedUser, assignedPersons);

        Task task = new Task();
        task.setTitle(taskDTO.getTitle());
        task.setDescription(taskDTO.getDescription());
        task.setAssignedPersons(assignedPersons);
        task.setStatus(taskDTO.getStatus());
        task.setDeadline(taskDTO.getDeadline());

        if (taskDTO.getStatus() == TaskStatus.DONE) {
            task.setCompletedAt(LocalDateTime.now());
        }

        return taskRepository.save(task);
    }

    public Task updateTask(UUID loggedUserId, UUID uuid, TaskCreateDTO taskDTO) throws ValidationException {
        Person loggedUser = authorizationService.requireLoggedUser(loggedUserId);

        Task existingTask = taskRepository.findById(uuid).orElseThrow(
                () -> new ValidationException("Task with id " + uuid + " not found")
        );

        authorizationService.canEditTask(loggedUser, existingTask);

        List<Person> assignedPersons = new ArrayList<>();

        for (UUID personId : taskDTO.getPersonIds()) {
            Person person = personRepository.findById(personId).orElseThrow(
                    () -> new ValidationException("Person with id " + personId + " not found")
            );
            assignedPersons.add(person);
        }

        // head-ul nu are voie sa faca task mixt
        if (loggedUser.getRole() == PersonRole.DEPARTMENT_HEAD) {
            authorizationService.canCreateTask(loggedUser, assignedPersons);
        }

        // member nu are voie full update
        if (loggedUser.getRole() == PersonRole.MEMBER) {
            throw new ValidationException("MEMBER cannot fully update tasks");
        }

        existingTask.setTitle(taskDTO.getTitle());
        existingTask.setDescription(taskDTO.getDescription());
        existingTask.setAssignedPersons(assignedPersons);

        if (existingTask.getStatus() == TaskStatus.DONE &&
                taskDTO.getStatus() != TaskStatus.DONE) {
            throw new ValidationException("A completed task cannot be changed back to TODO or IN_PROGRESS");
        }

        existingTask.setStatus(taskDTO.getStatus());
        existingTask.setDeadline(taskDTO.getDeadline());

        if (taskDTO.getStatus() == TaskStatus.DONE) {
            if (existingTask.getCompletedAt() == null) {
                existingTask.setCompletedAt(LocalDateTime.now());
            }
        } else {
            existingTask.setCompletedAt(null);
        }

        return taskRepository.save(existingTask);
    }

    public void deleteTask(UUID loggedUserId, UUID uuid) throws ValidationException {
        Person loggedUser = authorizationService.requireLoggedUser(loggedUserId);
        authorizationService.canDeleteTask(loggedUser);

        if (!taskRepository.existsById(uuid)) {
            throw new ValidationException("Task with id " + uuid + " not found");
        }

        taskRepository.deleteById(uuid);
    }

    public Task patchTask(UUID loggedUserId, UUID uuid, TaskPatchDTO taskPatchDTO) throws ValidationException {
        Person loggedUser = authorizationService.requireLoggedUser(loggedUserId);

        Task existingTask = taskRepository.findById(uuid).orElseThrow(
                () -> new ValidationException("Task with id " + uuid + " not found")
        );

        authorizationService.canEditTask(loggedUser, existingTask);

        boolean changesStatus = taskPatchDTO.getStatus() != null;

        boolean changesOtherFields =
                taskPatchDTO.getTitle() != null ||
                        taskPatchDTO.getDescription() != null ||
                        taskPatchDTO.getDeadline() != null ||
                        taskPatchDTO.getAddPersonIds() != null ||
                        taskPatchDTO.getRemovePersonIds() != null;

        authorizationService.ensureMemberCanOnlyChangeTaskStatus(
                loggedUser,
                existingTask,
                changesStatus,
                changesOtherFields
        );

        if (taskPatchDTO.getTitle() != null) {
            existingTask.setTitle(taskPatchDTO.getTitle());
        }

        if (taskPatchDTO.getDescription() != null) {
            existingTask.setDescription(taskPatchDTO.getDescription());
        }

        List<Person> assignedPersons = existingTask.getAssignedPersons();

        if (taskPatchDTO.getAddPersonIds() != null) {
            for (UUID personId : taskPatchDTO.getAddPersonIds()) {
                Person person = personRepository.findById(personId).orElseThrow(
                        () -> new ValidationException("Person with id " + personId + " not found")
                );

                boolean alreadyAssigned = assignedPersons.stream()
                        .anyMatch(existingPerson -> existingPerson.getId().equals(personId));

                if (!alreadyAssigned) {
                    assignedPersons.add(person);
                }
            }
        }

        if (taskPatchDTO.getRemovePersonIds() != null) {
            assignedPersons.removeIf(person ->
                    taskPatchDTO.getRemovePersonIds().contains(person.getId())
            );
        }

        // head-ul nu are voie sa lase task-ul sa devina mixt
        if (loggedUser.getRole() == PersonRole.DEPARTMENT_HEAD) {
            authorizationService.canCreateTask(loggedUser, assignedPersons);
        }

        existingTask.setAssignedPersons(assignedPersons);

        if (taskPatchDTO.getStatus() != null) {
            if (loggedUser.getRole() == PersonRole.MEMBER &&
                    taskPatchDTO.getStatus() == TaskStatus.DONE) {
                throw new ValidationException("MEMBER cannot mark task as DONE directly. Request approval instead.");
            }
            if (existingTask.getStatus() == TaskStatus.DONE &&
                    taskPatchDTO.getStatus() != TaskStatus.DONE) {
                throw new ValidationException("A completed task cannot be changed back to TODO or IN_PROGRESS");
            }

            existingTask.setStatus(taskPatchDTO.getStatus());

            if (taskPatchDTO.getStatus() == TaskStatus.DONE) {
                if (existingTask.getCompletedAt() == null) {
                    existingTask.setCompletedAt(LocalDateTime.now());
                }
            } else {
                existingTask.setCompletedAt(null);
            }
        }

        if (taskPatchDTO.getDeadline() != null) {
            existingTask.setDeadline(taskPatchDTO.getDeadline());
        }

        return taskRepository.save(existingTask);
    }

    public List<Task> getOverdueTasks() {
        return taskRepository.findByDeadlineBeforeAndStatusNot(LocalDateTime.now(), TaskStatus.DONE);
    }

    public List<Task> getInProgressTasks() {
        return taskRepository.findByStatus(TaskStatus.IN_PROGRESS);
    }

    public List<Task> getTasksByPersonId(UUID personId) throws ValidationException {
        if (!personRepository.existsById(personId)) {
            throw new ValidationException("Person with id " + personId + " not found");
        }

        return taskRepository.findTasksByPersonId(personId);
    }
}