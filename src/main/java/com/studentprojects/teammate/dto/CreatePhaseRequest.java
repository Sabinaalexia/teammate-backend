package com.studentprojects.teammate.dto;

import lombok.Data;

@Data
public class CreatePhaseRequest {
    private String name;
    private Integer difficulty;
    private Integer orderIndex;
}