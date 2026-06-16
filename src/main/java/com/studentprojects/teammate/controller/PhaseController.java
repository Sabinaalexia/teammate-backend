package com.studentprojects.teammate.controller;

import com.studentprojects.teammate.dto.PhaseResponse;
import com.studentprojects.teammate.service.JwtService;
import com.studentprojects.teammate.service.PhaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMethod;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"}, allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
@RequiredArgsConstructor
public class PhaseController {

    private final PhaseService phaseService;
    private final JwtService jwtService;

    @GetMapping("/projects/{projectId}/phases")
    public ResponseEntity<?> getProjectPhases(@PathVariable Long projectId,
                                              @RequestHeader("Authorization") String authHeader) {
        try {
            List<PhaseResponse> phases = phaseService.getProjectPhases(projectId);
            return ResponseEntity.ok(phases);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    // Scrumul confirmă sprintul
    @PostMapping("/phases/{phaseId}/confirm")
    public ResponseEntity<?> confirmPhase(@PathVariable Long phaseId,
                                          @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            Long userId = jwtService.getUserIdFromToken(token);
            PhaseResponse response = phaseService.confirmPhase(phaseId, userId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    // Notifică scrumul când toți membrii termină
    @PostMapping("/phases/{phaseId}/notify-complete")
    public ResponseEntity<?> notifyComplete(@PathVariable Long phaseId,
                                            @RequestHeader("Authorization") String authHeader) {
        try {
            phaseService.notifyAllTasksDone(phaseId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    record ErrorResponse(String error) {}
}