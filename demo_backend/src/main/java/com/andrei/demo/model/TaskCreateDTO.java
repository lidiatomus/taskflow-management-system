package com.andrei.demo.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class TaskCreateDTO {

    @NotBlank(message = "Title is required")
    @Size(min = 2, max = 100, message = "Title should be between 2 and 100 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(min = 2, max = 500, message = "Description should be between 2 and 500 characters")
    private String description;

    @NotEmpty(message = "At least one person must be assigned")
    private List<UUID> personIds;

    @NotNull(message = "Status is required")
    private TaskStatus status;

    @NotNull(message = "Deadline is required")
    private LocalDateTime deadline;
}