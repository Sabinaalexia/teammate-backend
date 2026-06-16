package com.studentprojects.teammate.service;

import com.studentprojects.teammate.entity.Notification;
import com.studentprojects.teammate.repository.NotificationRepository;
import com.studentprojects.teammate.repository.ProjectMemberRepository;
import com.studentprojects.teammate.entity.MemberStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final ProjectMemberRepository projectMemberRepository;

    // Trimite notificare unui singur user
    public void sendToUser(Long userId, String message, String type, Long projectId, Long taskId, Integer sprintIndex) {
        Notification n = Notification.builder()
                .userId(userId)
                .message(message)
                .type(type)
                .projectId(projectId)
                .taskId(taskId)
                .sprintIndex(sprintIndex)
                .read(false)
                .build();
        notificationRepository.save(n);
    }

    // Trimite notificare tuturor membrilor acceptați dintr-un proiect (exclusiv creatorId dacă exclude=true)
    public void sendToProjectMembers(Long projectId, Long excludeUserId, String message, String type, Integer sprintIndex) {
        projectMemberRepository.findByProjectId(projectId).stream()
                .filter(m -> m.getStatus() == MemberStatus.ACCEPTED)
                .filter(m -> !m.getUserId().equals(excludeUserId))
                .forEach(m -> sendToUser(m.getUserId(), message, type, projectId, null, sprintIndex));
    }

    // Ia notificările necitite ale unui user
    public List<Notification> getUnread(Long userId) {
        return notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId);
    }

    // Marchează o notificare ca citită
    public void markAsRead(Long notificationId) {
        notificationRepository.markAsRead(notificationId);
    }

    // Marchează toate ca citite
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsRead(userId);
    }
    public boolean existsSprintDoneNotification(Long projectId, Long creatorId, Integer sprintIndex) {
        return notificationRepository.existsByUserIdAndTypeAndProjectIdAndSprintIndex(
                creatorId, "SPRINT_DONE_SCRUM", projectId, sprintIndex
        );
    }
}