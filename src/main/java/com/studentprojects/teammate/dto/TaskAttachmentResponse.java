package com.studentprojects.teammate.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class TaskAttachmentResponse {
    private Long id;
    private Long taskId;
    private String fileName;
    private String uploadedByName;
    private LocalDateTime createdAt;
}