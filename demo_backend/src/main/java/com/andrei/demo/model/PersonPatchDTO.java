package com.andrei.demo.model;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;
import jakarta.validation.constraints.Email;

import java.util.UUID;

//clasa de patch, toate campurile sunt optionale, nu are validari, doar un DTO pt update partial
@Data
public class PersonPatchDTO {

    @Size(min = 2, max = 100, message = "Name should be between 2 and 100 characters")
    private String name;
    private String password;
    @Min(value = 0, message = "Age must be at least 0")
    @Max(value = 100, message = "Age must be less than or equal to 100")
    private Integer age;
    @Email(message = "Email must be valid")
    private String email;
    private PersonRole role;
    private UUID departamentId;

}