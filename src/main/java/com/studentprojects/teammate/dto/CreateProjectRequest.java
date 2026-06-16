package com.studentprojects.teammate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CreateProjectRequest {

    @NotBlank(message = "Numele proiectului este obligatoriu")
    @Size(min = 3, max = 100, message = "Numele trebuie să aibă între 3 și 100 caractere")
    private String name;

    private String description;
    private LocalDate startDate;
    private Integer bufferDays;

    @NotNull(message = "Data limită este obligatorie")
    private LocalDate deadline;

    private List<PhaseData> phases;

    @Data
    public static class PhaseData {
        private String name;
        private Integer difficulty;
        private Integer orderIndex;
    }
}