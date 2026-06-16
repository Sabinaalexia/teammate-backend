package com.studentprojects.teammate.dto;

import lombok.Data;
import java.util.List;

@Data
public class CreateTaskRequest {
    private String title;
    private String description;
    private List<Long> assignedTo; // Listă de ID-uri trimisă din React
}