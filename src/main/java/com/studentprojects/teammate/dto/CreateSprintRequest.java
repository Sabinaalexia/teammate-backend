package com.studentprojects.teammate.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class CreateSprintRequest {
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<Long> phaseIds; // etapele incluse în sprint
}