package com.andrei.demo.model;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class TaskPatchDTO {

    @Size(min = 2, max = 100, message = "Title should be between 2 and 100 characters")
    private String title;

    @Size(min = 2, max = 500, message = "Description should be between 2 and 500 characters")
    private String description;

    private List<UUID> addPersonIds;
    private List<UUID> removePersonIds;
    //putem inlocui doar o persoana pe care o vrem din lista de persoane
    //sau putem reasigna un nou grup de persoane inloc de cel vechi
    private TaskStatus status;
    private LocalDateTime deadline;
}