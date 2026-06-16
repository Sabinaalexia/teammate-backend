package com.studentprojects.teammate.repository;

import com.studentprojects.teammate.entity.Sprint;
import com.studentprojects.teammate.entity.SprintStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
public interface SprintRepository extends JpaRepository<Sprint, Long> {

    List<Sprint> findByProjectIdOrderByOrderIndexAsc(Long projectId);

    Optional<Sprint> findByProjectIdAndStatus(Long projectId, SprintStatus status);

    @Modifying
    @Transactional
    @Query("DELETE FROM Sprint s WHERE s.projectId = :projectId")
    void deleteByProjectId(@Param("projectId") Long projectId);
}