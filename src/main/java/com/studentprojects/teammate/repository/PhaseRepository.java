package com.studentprojects.teammate.repository;

import com.studentprojects.teammate.entity.Phase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository
public interface PhaseRepository extends JpaRepository<Phase, Long> {

    List<Phase> findByProjectIdOrderByOrderIndexAsc(Long projectId);
    List<Phase> findBySprintId(Long sprintId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Phase p WHERE p.projectId = :projectId")
    void deleteByProjectId(@Param("projectId") Long projectId);
}