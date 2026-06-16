package com.studentprojects.teammate.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class TaskCommentResponse {
    private Long id;
    private Long taskId;
    private Long userId;
    private String userName;
    private String message;
    private LocalDateTime createdAt;
}