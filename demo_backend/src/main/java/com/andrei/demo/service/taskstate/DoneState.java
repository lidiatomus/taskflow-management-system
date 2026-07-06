package com.andrei.demo.service.taskstate;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.Person;
import com.andrei.demo.model.Task;

public class DoneState implements TaskState {

    @Override
    public void requestApproval(
            Task task,
            Person user
    ) throws ValidationException {

        throw new ValidationException(
                "Task already completed"
        );
    }

    @Override
    public void approve(
            Task task,
            Person user
    ) throws ValidationException {

        throw new ValidationException(
                "Task already completed"
        );
    }

    @Override
    public void reject(
            Task task,
            Person user,
            String comment
    ) throws ValidationException {

        throw new ValidationException(
                "Task already completed"
        );
    }
}