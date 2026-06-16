package com.studentprojects.teammate.dto;

import com.studentprojects.teammate.entity.ProjectStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponse {
    private Long id;
    private String name;
    private String description;
    private LocalDate deadline;
    private LocalDate startDate;
    private ProjectStatus status;
    private Long creatorId;
    private String creatorName;
    private String creatorUsername;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<String> members;
}
