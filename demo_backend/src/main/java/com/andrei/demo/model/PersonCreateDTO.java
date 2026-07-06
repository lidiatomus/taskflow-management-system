package com.andrei.demo.model;

import com.andrei.demo.validator.StrongPassword;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.UUID;
import jakarta.validation.constraints.NotNull;

@Data
public class PersonCreateDTO {
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message =
            "Name should be between 2 and 100 characters")
    private String name;

    @Min(value = 0, message = "Age must be at least 0")
    @Max(value = 100, message = "Age must be less than or equal to 100")
    @NotNull(message = "Age is required")
    private Integer age;


    @Email(message = "Email must be valid") //springboot are built in validator
    @NotBlank(message = "Email is required")
    private String email;

    @NotNull(message = "Role is required")
    private PersonRole role;

    @NotNull(message = "Departament id is required")
    private UUID departamentId;
}
