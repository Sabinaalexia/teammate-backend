package com.studentprojects.teammate.controller;

import com.studentprojects.teammate.dto.CreateProjectRequest;
import com.studentprojects.teammate.dto.ProjectResponse;
import com.studentprojects.teammate.service.JwtService;
import com.studentprojects.teammate.service.ProjectService;
import com.studentprojects.teammate.service.SprintService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"}, allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final SprintService sprintService;
    private final JwtService jwtService;

    @PostMapping
    public ResponseEntity<?> createProject(
            @RequestBody CreateProjectRequest request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            Long userId = jwtService.getUserIdFromToken(token);
            ProjectResponse response = projectService.createProject(request, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getUserProjects(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            Long userId = jwtService.getUserIdFromToken(token);
            List<ProjectResponse> projects = projectService.getUserProjects(userId);
            return ResponseEntity.ok(projects);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProjectById(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            Long userId = jwtService.getUserIdFromToken(token);
            ProjectResponse project = projectService.getProjectById(id, userId);
            return ResponseEntity.ok(project);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProject(
            @PathVariable Long id,
            @RequestBody CreateProjectRequest request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            Long userId = jwtService.getUserIdFromToken(token);
            ProjectResponse response = projectService.updateProject(id, request, userId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProject(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            Long userId = jwtService.getUserIdFromToken(token);
            projectService.deleteProject(id, userId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    // Notifică scrum-ul când toți membrii termină taskurile
    @PostMapping("/{projectId}/phases/{phaseIndex}/notify-complete")
    public ResponseEntity<?> notifySprintComplete(
            @PathVariable Long projectId,
            @PathVariable Integer phaseIndex,
            @RequestHeader("Authorization") String authHeader) {
        try {
            sprintService.notifySprintComplete(projectId, Long.valueOf(phaseIndex));
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    @PutMapping("/{id}/archive")
    public ResponseEntity<?> archiveProject(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            Long userId = jwtService.getUserIdFromToken(token);
            ProjectResponse response = projectService.archiveProject(id, userId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/{id}/unarchive")
    public ResponseEntity<?> unarchiveProject(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            Long userId = jwtService.getUserIdFromToken(token);
            ProjectResponse response = projectService.unarchiveProject(id, userId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    record ErrorResponse(String error) {}
}