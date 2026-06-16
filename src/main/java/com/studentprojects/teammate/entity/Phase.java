package com.studentprojects.teammate.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "phases")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Phase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sprint_id")
    private Long sprintId;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(nullable = false)
    private String name;

    private Integer difficulty;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PhaseStatus status = PhaseStatus.NOT_STARTED;

    @Column(name = "order_index")
    private Integer orderIndex;

    @Column(name = "confirmed_by_scrum", nullable = false)
    @Builder.Default
    private boolean confirmedByScrum = false;

    // ── NOU ──────────────────────────────────────────────
    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;
    // ─────────────────────────────────────────────────────
}