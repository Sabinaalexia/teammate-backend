package com.studentprojects.teammate.repository;

import com.studentprojects.teammate.entity.TaskComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TaskCommentRepository extends JpaRepository<TaskComment, Long> {

    @Query(value = "SELECT * FROM task_comments WHERE task_id = :taskId ORDER BY created_at ASC", nativeQuery = true)
    List<TaskComment> findByTaskId(@Param("taskId") Long taskId);
}