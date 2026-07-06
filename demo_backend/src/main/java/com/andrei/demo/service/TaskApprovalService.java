package com.andrei.demo.service;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.Person;
import com.andrei.demo.model.PersonRole;
import com.andrei.demo.model.Task;
import com.andrei.demo.repository.TaskRepository;
import com.andrei.demo.service.taskstate.TaskState;
import com.andrei.demo.service.taskstate.TaskStateFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskApprovalService {

    private final TaskRepository taskRepository;
    private final AuthorizationService authorizationService;
    private final TaskStateFactory taskStateFactory;
    private final AuditLogService auditLogService;

    public Task requestApproval(
            UUID loggedUserId,
            UUID taskId
    ) throws ValidationException {

        Person user =
                authorizationService
                        .requireLoggedUser(
                                loggedUserId
                        );

        Task task =
                taskRepository.findById(taskId)
                        .orElseThrow(() ->
                                new ValidationException(
                                        "Task not found"
                                ));

        if(user.getRole()!=PersonRole.MEMBER){

            throw new ValidationException(
                    "Only MEMBER can request approval"
            );
        }

        TaskState state =
                taskStateFactory
                        .getState(
                                task.getStatus()
                        );

        state.requestApproval(
                task,
                user
        );

        auditLogService.log(
                user,
                "TASK_APPROVAL_REQUESTED",
                "TASK",
                task.getId(),
                user.getName() +
                        " requested approval for task " +
                        task.getTitle()
        );

        return taskRepository.save(task);
    }


    public Task approve(
            UUID loggedUserId,
            UUID taskId
    ) throws ValidationException {

        Person user =
                authorizationService
                        .requireLoggedUser(
                                loggedUserId
                        );

        Task task =
                taskRepository.findById(taskId)
                        .orElseThrow(() ->
                                new ValidationException(
                                        "Task not found"
                                ));

        if(
                user.getRole()!=PersonRole.DEPARTMENT_HEAD
                        &&
                        user.getRole()!=PersonRole.TEAM_MANAGER
        ){

            throw new ValidationException(
                    "Not allowed"
            );
        }

        TaskState state =
                taskStateFactory
                        .getState(
                                task.getStatus()
                        );

        state.approve(
                task,
                user
        );

        auditLogService.log(
                user,
                "TASK_APPROVAL_REQUESTED",
                "TASK",
                task.getId(),
                user.getName() +
                        " requested approval for task " +
                        task.getTitle()
        );

        return taskRepository.save(task);
    }



    public Task reject(
            UUID loggedUserId,
            UUID taskId,
            String comment
    ) throws ValidationException {

        Person user =
                authorizationService
                        .requireLoggedUser(
                                loggedUserId
                        );

        Task task =
                taskRepository.findById(taskId)
                        .orElseThrow(() ->
                                new ValidationException(
                                        "Task not found"
                                ));

        if(
                user.getRole()!=PersonRole.DEPARTMENT_HEAD
                        &&
                        user.getRole()!=PersonRole.TEAM_MANAGER
        ){

            throw new ValidationException(
                    "Not allowed"
            );
        }

        TaskState state =
                taskStateFactory
                        .getState(
                                task.getStatus()
                        );

        state.reject(
                task,
                user,
                comment
        );
        auditLogService.log(
                user,
                "TASK_APPROVAL_REQUESTED",
                "TASK",
                task.getId(),
                user.getName() +
                        " requested approval for task " +
                        task.getTitle()
        );
        return taskRepository.save(task);
    }
}