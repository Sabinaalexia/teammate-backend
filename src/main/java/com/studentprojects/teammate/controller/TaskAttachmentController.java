package com.studentprojects.teammate.controller;

import com.studentprojects.teammate.dto.TaskAttachmentResponse;
import com.studentprojects.teammate.entity.TaskAttachment;
import com.studentprojects.teammate.entity.User;
import com.studentprojects.teammate.repository.TaskAttachmentRepository;
import com.studentprojects.teammate.repository.UserRepository;
import com.studentprojects.teammate.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"}, allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
@RequiredArgsConstructor
public class TaskAttachmentController {

    private final TaskAttachmentRepository attachmentRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @PostMapping("/tasks/{taskId}/attachments")
    public ResponseEntity<?> uploadAttachment(
            @PathVariable Long taskId,
            @RequestParam("file") MultipartFile file,
            @RequestHeader("Authorization") String authHeader) {
        try {
            if (!file.getOriginalFilename().toLowerCase().endsWith(".pdf")) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Doar fișiere PDF sunt acceptate."));
            }
            if (file.getSize() > 10 * 1024 * 1024) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Fișierul depășește 10MB."));
            }

            String token = authHeader.replace("Bearer ", "");
            Long userId = jwtService.getUserIdFromToken(token);
            User user = userRepository.findById(userId).orElse(null);

            TaskAttachment attachment = TaskAttachment.builder()
                    .taskId(taskId)
                    .fileName(file.getOriginalFilename())
                    .fileData(file.getBytes())
                    .uploadedBy(userId)
                    .uploadedByName(user != null ? user.getName() : "Necunoscut")
                    .build();

            TaskAttachment saved = attachmentRepository.save(attachment);
            return ResponseEntity.ok(toResponse(saved));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/tasks/{taskId}/attachments")
    public ResponseEntity<?> getAttachments(@PathVariable Long taskId,
                                            @RequestHeader("Authorization") String authHeader) {
        List<TaskAttachmentResponse> list = attachmentRepository.findByTaskId(taskId)
                .stream().map(this::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/attachments/{id}/download")
    public ResponseEntity<byte[]> downloadAttachment(@PathVariable Long id,
                                                     @RequestHeader("Authorization") String authHeader) {
        TaskAttachment attachment = attachmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fișierul nu a fost găsit."));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + attachment.getFileName() + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(attachment.getFileData());
    }

    @DeleteMapping("/attachments/{id}")
    public ResponseEntity<?> deleteAttachment(@PathVariable Long id,
                                              @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            Long userId = jwtService.getUserIdFromToken(token);
            TaskAttachment attachment = attachmentRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Fișierul nu a fost găsit."));
            if (!attachment.getUploadedBy().equals(userId)) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Doar cel care a încărcat poate șterge fișierul."));
            }
            attachmentRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    private TaskAttachmentResponse toResponse(TaskAttachment a) {
        return TaskAttachmentResponse.builder()
                .id(a.getId())
                .taskId(a.getTaskId())
                .fileName(a.getFileName())
                .uploadedByName(a.getUploadedByName())
                .createdAt(a.getCreatedAt())
                .build();
    }

    record ErrorResponse(String error) {}
}