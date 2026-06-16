package com.studentprojects.teammate.controller;

import com.studentprojects.teammate.dto.TaskCommentResponse;
import com.studentprojects.teammate.service.JwtService;
import com.studentprojects.teammate.service.TaskCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"}, allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
                RequestMethod.DELETE, RequestMethod.OPTIONS})
@RequiredArgsConstructor
public class TaskCommentController {

    private final TaskCommentService taskCommentService;
    private final JwtService jwtService;

    @GetMapping("/tasks/{taskId}/comments")
    public ResponseEntity<?> getComments(@PathVariable Long taskId) {
        List<TaskCommentResponse> comments = taskCommentService.getComments(taskId);
        return ResponseEntity.ok(comments);
    }

    @PostMapping("/tasks/{taskId}/comments")
    public ResponseEntity<?> addComment(
            @PathVariable Long taskId,
            @RequestBody Map<String, String> body,
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            String token = authHeader.replace("Bearer ", "");
            Long userId = jwtService.getUserIdFromToken(token);
            String message = body.get("message");
            if (message == null || message.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Mesajul nu poate fi gol."));
            }
            TaskCommentResponse response = taskCommentService.addComment(taskId, userId, message.trim());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}