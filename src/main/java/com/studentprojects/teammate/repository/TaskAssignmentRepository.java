package com.studentprojects.teammate.repository;

import com.studentprojects.teammate.entity.TaskAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskAssignmentRepository extends JpaRepository<TaskAssignment, Long> {

    @org.springframework.data.jpa.repository.Query(value = "SELECT * FROM task_assignments WHERE task_id = :taskId", nativeQuery = true)
    List<TaskAssignment> findByTaskId(@org.springframework.data.repository.query.Param("taskId") Long taskId);

    void deleteByTaskId(Long taskId);

    boolean existsByTaskIdAndUserId(Long taskId, Long userId);
}