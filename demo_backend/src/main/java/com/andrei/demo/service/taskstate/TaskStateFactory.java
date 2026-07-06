package com.andrei.demo.service.taskstate;

import com.andrei.demo.model.TaskStatus;
import org.springframework.stereotype.Component;

@Component
public class TaskStateFactory {

    public TaskState getState(TaskStatus status) {

        return switch (status) {

            case IN_PROGRESS ->
                    new InProgressState();

            case PENDING_APPROVAL ->
                    new PendingApprovalState();

            case DONE ->
                    new DoneState();

            default ->
                    throw new RuntimeException(
                            "Unsupported task state"
                    );
        };
    }
}