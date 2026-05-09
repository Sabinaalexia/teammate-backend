package com.studentprojects.teammate.controller;

import com.studentprojects.teammate.dto.CreateProjectRequest;
import com.studentprojects.teammate.dto.ProjectResponse;
import com.studentprojects.teammate.service.JwtService;
import com.studentprojects.teammate.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final JwtService jwtService;

    // Creează proiect nou
    @PostMapping
    public ResponseEntity<?> createProject(
            @Valid @RequestBody CreateProjectRequest request,
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            // Extrage userId din token JWT
            String token = authHeader.replace("Bearer ", "");
            Long userId = jwtService.getUserIdFromToken(token);

            // Creează proiect
            ProjectResponse response = projectService.createProject(request, userId);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    // Ia toate proiectele user-ului autentificat
    @GetMapping
    public ResponseEntity<?> getUserProjects(
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            // Extrage userId din token JWT
            String token = authHeader.replace("Bearer ", "");
            Long userId = jwtService.getUserIdFromToken(token);

            // Ia proiectele
            List<ProjectResponse> projects = projectService.getUserProjects(userId);

            return ResponseEntity.ok(projects);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    // Ia detalii proiect specific
    @GetMapping("/{id}")
    public ResponseEntity<?> getProjectById(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            // Extrage userId din token JWT
            String token = authHeader.replace("Bearer ", "");
            Long userId = jwtService.getUserIdFromToken(token);

            // Ia proiectul
            ProjectResponse project = projectService.getProjectById(id, userId);

            return ResponseEntity.ok(project);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    // Clasă internă pentru răspunsuri eroare
    record ErrorResponse(String error) {}
}