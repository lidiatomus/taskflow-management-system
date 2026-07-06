package com.andrei.demo.service.taskstate;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.Person;
import com.andrei.demo.model.Task;
import com.andrei.demo.model.TaskStatus;

import java.time.LocalDateTime;

public class InProgressState implements TaskState {

    @Override
    public void requestApproval(Task task, Person user)
            throws ValidationException {

        task.setStatus(TaskStatus.PENDING_APPROVAL);
        task.setApprovalRequestedAt(LocalDateTime.now());
    }

    @Override
    public void approve(Task task, Person user)
            throws ValidationException {

        throw new ValidationException(
                "Task must be pending approval"
        );
    }

    @Override
    public void reject(
            Task task,
            Person user,
            String comment
    ) throws ValidationException {

        throw new ValidationException(
                "Task must be pending approval"
        );
    }
}