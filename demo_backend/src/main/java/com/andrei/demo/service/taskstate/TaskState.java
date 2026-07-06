package com.andrei.demo.service.taskstate;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.Person;
import com.andrei.demo.model.Task;

public interface TaskState {

    void requestApproval(Task task, Person user)
            throws ValidationException;

    void approve(Task task, Person user)
            throws ValidationException;

    void reject(Task task, Person user, String comment)
            throws ValidationException;
}