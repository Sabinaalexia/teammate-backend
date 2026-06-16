package com.studentprojects.teammate.repository;

import com.studentprojects.teammate.entity.TaskAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TaskAttachmentRepository extends JpaRepository<TaskAttachment, Long> {
    List<TaskAttachment> findByTaskId(Long taskId);
}