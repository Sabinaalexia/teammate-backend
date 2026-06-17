package com.studentprojects.teammate.controller;

import com.studentprojects.teammate.entity.SprintFeedback;
import com.studentprojects.teammate.repository.SprintFeedbackRepository;
import com.studentprojects.teammate.repository.UserRepository;
import com.studentprojects.teammate.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"}, allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.OPTIONS})
@RequiredArgsConstructor
public class SprintFeedbackController {

    private final SprintFeedbackRepository feedbackRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @GetMapping("/api/phases/{phaseId}/feedbacks")
    public ResponseEntity<List<SprintFeedback>> getFeedbacks(@PathVariable Long phaseId) {
        return ResponseEntity.ok(feedbackRepository.findByPhaseId(phaseId));
    }

    @PostMapping("/api/phases/{phaseId}/feedbacks")
    public ResponseEntity<?> addFeedback(
            @PathVariable Long phaseId,
            @RequestBody Map<String, String> body,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            Long userId = jwtService.getUserIdFromToken(token);
            var user = userRepository.findById(userId).orElse(null);

            SprintFeedback feedback = SprintFeedback.builder()
                    .phaseId(phaseId)
                    .userId(userId)
                    .userName(user != null ? user.getName() : "Anonim")
                    .ceAMersBine(body.get("ceAMersBine"))
                    .deImbunatatit(body.get("deImbunatatit"))
                    .build();

            return ResponseEntity.ok(feedbackRepository.save(feedback));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/api/feedbacks/{feedbackId}")
    public ResponseEntity<?> updateFeedback(
            @PathVariable Long feedbackId,
            @RequestBody Map<String, String> body,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            Long userId = jwtService.getUserIdFromToken(token);

            SprintFeedback feedback = feedbackRepository.findById(feedbackId).orElse(null);
            if (feedback == null) return ResponseEntity.notFound().build();
            if (!feedback.getUserId().equals(userId))
                return ResponseEntity.status(403).body(Map.of("error", "Nu poti modifica feedback-ul altcuiva."));

            feedback.setCeAMersBine(body.get("ceAMersBine"));
            feedback.setDeImbunatatit(body.get("deImbunatatit"));

            return ResponseEntity.ok(feedbackRepository.save(feedback));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}