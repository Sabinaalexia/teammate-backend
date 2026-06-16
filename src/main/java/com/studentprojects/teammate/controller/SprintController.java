package com.studentprojects.teammate.controller;

import com.studentprojects.teammate.dto.CreateSprintRequest;
import com.studentprojects.teammate.dto.SprintResponse;
import com.studentprojects.teammate.service.JwtService;
import com.studentprojects.teammate.service.SprintService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/sprints")
@CrossOrigin(origins = "http://localhost:5173", allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
                RequestMethod.DELETE, RequestMethod.OPTIONS})
@RequiredArgsConstructor
public class SprintController {

    private final SprintService sprintService;
    private final JwtService jwtService;

    @PostMapping
    public ResponseEntity<?> createSprint(
            @PathVariable Long projectId,
            @RequestBody CreateSprintRequest request,
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            String token = authHeader.replace("Bearer ", "");
            Long userId = jwtService.getUserIdFromToken(token);
            SprintResponse response = sprintService.createSprint(projectId, request, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getProjectSprints(
            @PathVariable Long projectId,
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            List<SprintResponse> sprints = sprintService.getProjectSprints(projectId);
            return ResponseEntity.ok(sprints);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/active")
    public ResponseEntity<?> getActiveSprint(
            @PathVariable Long projectId,
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            SprintResponse sprint = sprintService.getActiveSprint(projectId);
            return ResponseEntity.ok(sprint);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/{sprintId}/confirm")
    public ResponseEntity<?> confirmSprint(
            @PathVariable Long projectId,
            @PathVariable Long sprintId,
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            String token = authHeader.replace("Bearer ", "");
            Long userId = jwtService.getUserIdFromToken(token);
            SprintResponse response = sprintService.confirmSprint(sprintId, userId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/{sprintId}/check-completion")
    public ResponseEntity<?> checkCompletion(
            @PathVariable Long projectId,
            @PathVariable Long sprintId,
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            boolean allDone = sprintService.checkAllTasksDone(sprintId);
            return ResponseEntity.ok(allDone);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    record ErrorResponse(String error) {}
}