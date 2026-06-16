package com.studentprojects.teammate.dto;

import com.studentprojects.teammate.entity.SprintStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class SprintResponse {
    private Long id;
    private Long projectId;
    private String name;
    private Integer orderIndex;
    private LocalDate startDate;
    private LocalDate endDate;
    private SprintStatus status;
    private LocalDateTime confirmedAt;
    private List<PhaseResponse> phases; // etapele din acest sprint
    private Integer progressPercent; // 0-100 calculat din taskuri
    private boolean allTasksDone; // toți membrii au bifat done
}