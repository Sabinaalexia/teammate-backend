package com.studentprojects.teammate.repository;

import com.studentprojects.teammate.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserIdAndReadFalseOrderByCreatedAtDesc(Long userId);

    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.read = true WHERE n.userId = :userId")
    void markAllAsRead(@Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.read = true WHERE n.id = :id")
    void markAsRead(@Param("id") Long id);

    boolean existsByUserIdAndTypeAndProjectIdAndSprintIndex(
            Long userId, String type, Long projectId, Integer sprintIndex
    );
}