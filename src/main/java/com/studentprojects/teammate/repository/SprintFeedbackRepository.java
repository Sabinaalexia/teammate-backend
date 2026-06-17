package com.studentprojects.teammate.repository;

import com.studentprojects.teammate.entity.SprintFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SprintFeedbackRepository extends JpaRepository<SprintFeedback, Long> {
    List<SprintFeedback> findByPhaseId(Long phaseId);
    boolean existsByPhaseIdAndUserId(Long phaseId, Long userId);
}