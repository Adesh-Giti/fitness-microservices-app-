package com.fitness.userservice.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RequestDTO {
    @NotNull(message = "email should be null")
    @Email(message = "Invalid email format")
    private String email;

    @NotNull(message = "password should not be null")
    private String password;
    private  String firstName;
    private String lastName;
}
