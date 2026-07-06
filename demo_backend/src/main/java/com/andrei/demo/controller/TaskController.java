package com.andrei.demo.controller;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.*;
import com.andrei.demo.service.TaskApprovalService;
import com.andrei.demo.service.TaskService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@CrossOrigin
public class TaskController {

    private final TaskService taskService;
    private final TaskApprovalService taskApprovalService;

    @GetMapping("/task")
    public List<Task> getTasks() {
        return taskService.getTasks();
    }

    @GetMapping("/task/{uuid}")
    public Task getTaskById(@PathVariable UUID uuid) throws ValidationException {
        return taskService.getTaskById(uuid);
    }

    @PostMapping("/task")
    public Task addTask(
            @AuthenticationPrincipal Person loggedUser,
            @Valid @RequestBody TaskCreateDTO taskDTO
    ) throws ValidationException {
        return taskService.addTask(loggedUser.getId(), taskDTO);
    }

    @PutMapping("/task/{uuid}")
    public Task updateTask(
            @AuthenticationPrincipal Person loggedUser,
            @PathVariable UUID uuid,
            @Valid @RequestBody TaskCreateDTO taskDTO
    ) throws ValidationException {
        return taskService.updateTask(loggedUser.getId(), uuid, taskDTO);
    }

    @DeleteMapping("/task/{uuid}")
    public void deleteTask(
            @AuthenticationPrincipal Person loggedUser,
            @PathVariable UUID uuid
    ) throws ValidationException {
        taskService.deleteTask(loggedUser.getId(), uuid);
    }

    @PatchMapping("/task/{uuid}")
    public Task patchTask(
            @AuthenticationPrincipal Person loggedUser,
            @PathVariable UUID uuid,
            @Valid @RequestBody TaskPatchDTO taskPatchDTO
    ) throws ValidationException {
        return taskService.patchTask(loggedUser.getId(), uuid, taskPatchDTO);
    }

    @GetMapping("/task/overdue")
    public List<Task> getOverdueTasks() {
        return taskService.getOverdueTasks();
    }

    @GetMapping("/task/in-progress")
    public List<Task> getInProgressTasks() {
        return taskService.getInProgressTasks();
    }

    @GetMapping("/task/person/{uuid}")
    public List<Task> getTasksByPersonId(@PathVariable UUID uuid) throws ValidationException {
        return taskService.getTasksByPersonId(uuid);
    }

    @PatchMapping("/task/{id}/request-approval")
    public Task requestApproval(
            @AuthenticationPrincipal Person loggedUser,
            @PathVariable UUID id
    ) throws ValidationException {
        return taskApprovalService.requestApproval(loggedUser.getId(), id);
    }

    @PatchMapping("/task/{id}/approve")
    public Task approve(
            @AuthenticationPrincipal Person loggedUser,
            @PathVariable UUID id
    ) throws ValidationException {
        return taskApprovalService.approve(loggedUser.getId(), id);
    }

    @PatchMapping("/task/{id}/reject")
    public Task reject(
            @AuthenticationPrincipal Person loggedUser,
            @PathVariable UUID id,
            @RequestBody TaskApprovalDTO dto
    ) throws ValidationException {
        return taskApprovalService.reject(loggedUser.getId(), id, dto.getComment());
    }
}