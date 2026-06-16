package com.studentprojects.teammate.dto;

import com.studentprojects.teammate.entity.PhaseStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class PhaseResponse {
    private Long id;
    private Long projectId;
    private String name;
    private Integer difficulty;
    private PhaseStatus status;
    private Integer orderIndex;
    private boolean confirmedByScrum;
    private List<TaskResponse> tasks;
    // ── NOU ──
    private LocalDate startDate;
    private LocalDate endDate;
    // ─────────
}