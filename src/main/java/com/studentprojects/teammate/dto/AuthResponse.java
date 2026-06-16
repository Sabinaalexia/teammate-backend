package com.studentprojects.teammate.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private Long id;
    private String email;
    private String username;
    private String name;
    private String token;
    private String message;
}