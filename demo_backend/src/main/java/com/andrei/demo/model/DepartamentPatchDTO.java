package com.andrei.demo.model;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DepartamentPatchDTO {

    private DepartamentName name;

    @Size(min = 10, max = 500, message = "Description should be between 10 and 500 characters")
    private String description;
}