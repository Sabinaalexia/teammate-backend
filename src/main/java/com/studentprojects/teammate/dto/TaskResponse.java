package com.studentprojects.teammate.dto;

import com.studentprojects.teammate.entity.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {
    private Long id;
    private Long phaseId;
    private Long projectId;
    private Long sprintId;
    private String title;
    private String description;
    private TaskStatus status;
    private Long createdBy;

    // Asigură-te că numele este scris exact așa (cu camelCase)
    private String createdByName;

    private LocalDateTime createdAt;
    private List<Long> assignedTo;
    private List<String> assignedToNames;
}