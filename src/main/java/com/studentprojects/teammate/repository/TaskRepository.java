package com.studentprojects.teammate.repository;

import com.studentprojects.teammate.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByPhaseIdOrderByCreatedAtAsc(Long phaseId);

    List<Task> findByProjectId(Long projectId);


    List<Task> findByPhaseId(Long phaseId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Task t WHERE t.phaseId = :phaseId")
    void deleteByPhaseId(@Param("phaseId") Long phaseId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Task t WHERE t.projectId = :projectId")
    void deleteByProjectId(@Param("projectId") Long projectId);
}