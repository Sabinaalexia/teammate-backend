package com.studentprojects.teammate.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sprint_feedbacks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SprintFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "phase_id", nullable = false)
    private Long phaseId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "ce_a_mers_bine", columnDefinition = "TEXT")
    private String ceAMersBine;

    @Column(name = "de_imbunatatit", columnDefinition = "TEXT")
    private String deImbunatatit;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}