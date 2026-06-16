package com.studentprojects.teammate.dto;

import com.studentprojects.teammate.entity.TaskStatus;
import lombok.Data;

@Data
public class UpdateTaskRequest {
    private String title;
    private String description;
    private java.util.List<Long> assignedTo;
    private TaskStatus status;
}