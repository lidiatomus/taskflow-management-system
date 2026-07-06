package com.andrei.demo.service.taskstate;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.Person;
import com.andrei.demo.model.Task;
import com.andrei.demo.model.TaskStatus;

import java.time.LocalDateTime;

public class PendingApprovalState implements TaskState {

    @Override
    public void requestApproval(Task task, Person user)
            throws ValidationException {

        throw new ValidationException(
                "Already waiting approval"
        );
    }

    @Override
    public void approve(Task task, Person user)
            throws ValidationException {

        task.setStatus(TaskStatus.DONE);
        task.setCompletedAt(LocalDateTime.now());
        task.setApprovalResolvedAt(LocalDateTime.now());
    }

    @Override
    public void reject(
            Task task,
            Person user,
            String comment
    ) throws ValidationException {

        task.setStatus(TaskStatus.IN_PROGRESS);

        task.setApprovalComment(comment);

        task.setApprovalResolvedAt(
                LocalDateTime.now()
        );
    }
}