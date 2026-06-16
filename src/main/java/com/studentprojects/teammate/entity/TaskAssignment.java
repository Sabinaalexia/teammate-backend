package com.studentprojects.teammate.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "task_assignments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Column(name = "user_id", nullable = false)
    private Long userId;
}