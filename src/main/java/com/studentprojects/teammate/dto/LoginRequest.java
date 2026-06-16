package com.studentprojects.teammate.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "Email sau username este obligatoriu")
    private String emailOrUsername;

    @NotBlank(message = "Parola este obligatorie")
    private String password;
}