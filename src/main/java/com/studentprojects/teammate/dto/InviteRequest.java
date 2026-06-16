package com.studentprojects.teammate.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class InviteRequest {

    @NotBlank(message = "Username sau email este obligatoriu")
    private String emailOrUsername;
}
