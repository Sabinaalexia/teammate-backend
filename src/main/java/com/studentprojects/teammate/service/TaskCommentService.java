package com.studentprojects.teammate.service;

import com.studentprojects.teammate.dto.TaskCommentResponse;
import com.studentprojects.teammate.entity.TaskComment;
import com.studentprojects.teammate.repository.TaskCommentRepository;
import com.studentprojects.teammate.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskCommentService {

    private final TaskCommentRepository taskCommentRepository;
    private final UserRepository userRepository;

    public TaskCommentResponse addComment(Long taskId, Long userId, String message) {
        TaskComment comment = TaskComment.builder()
                .taskId(taskId)
                .userId(userId)
                .message(message)
                .build();
        TaskComment saved = taskCommentRepository.save(comment);
        return convertToResponse(saved);
    }

    public List<TaskCommentResponse> getComments(Long taskId) {
        return taskCommentRepository.findByTaskId(taskId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private TaskCommentResponse convertToResponse(TaskComment comment) {
        var user = userRepository.findById(comment.getUserId()).orElse(null);
        return TaskCommentResponse.builder()
                .id(comment.getId())
                .taskId(comment.getTaskId())
                .userId(comment.getUserId())
                .userName(user != null ? user.getName() : "Utilizator șters")
                .message(comment.getMessage())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}