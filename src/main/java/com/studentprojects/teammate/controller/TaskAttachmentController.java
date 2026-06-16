package com.studentprojects.teammate.controller;

import com.studentprojects.teammate.entity.TaskAttachment;
import com.studentprojects.teammate.repository.TaskAttachmentRepository;
import com.studentprojects.teammate.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Field;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173", allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
                RequestMethod.DELETE, RequestMethod.OPTIONS})
@RequiredArgsConstructor
public class TaskAttachmentController {

    private final TaskAttachmentRepository taskAttachmentRepository;
    private final JwtService jwtService;

    @PostMapping("/tasks/{taskId}/attachments")
    public ResponseEntity<?> uploadAttachment(
            @PathVariable Long taskId,
            @RequestParam("file") MultipartFile file,
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            String token = authHeader.replace("Bearer ", "");
            Long userId = jwtService.getUserIdFromToken(token);

            String uploadDir = "./uploads/";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            TaskAttachment attachment = TaskAttachment.builder()
                    .taskId(taskId)
                    .fileName(file.getOriginalFilename())
                    .uploadedBy(userId)
                    .uploadedByName("Stefan")
                    .createdAt(LocalDateTime.now())
                    .build();

            for (Field field : TaskAttachment.class.getDeclaredFields()) {
                if (field.getType().equals(String.class) &&
                        (field.getName().toLowerCase().contains("path") || field.getName().toLowerCase().contains("url"))) {
                    field.setAccessible(true);
                    field.set(attachment, filePath.toString());
                    break;
                }
            }

            taskAttachmentRepository.save(attachment);
            return ResponseEntity.ok(attachment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/tasks/{taskId}/attachments")
    public ResponseEntity<?> getAttachments(@PathVariable Long taskId) {
        try {
            List<TaskAttachment> attachments = taskAttachmentRepository.findByTaskId(taskId);
            return ResponseEntity.ok(attachments);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\": \"Eroare la preluare\"}");
        }
    }

    @GetMapping("/attachments/{attId}/download")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable Long attId) {
        try {
            TaskAttachment attachment = taskAttachmentRepository.findById(attId)
                    .orElseThrow(() -> new RuntimeException("Fișierul nu există"));

            String pathValue = "";
            for (Field field : TaskAttachment.class.getDeclaredFields()) {
                if (field.getType().equals(String.class) &&
                        (field.getName().toLowerCase().contains("path") || field.getName().toLowerCase().contains("url"))) {
                    field.setAccessible(true);
                    pathValue = (String) field.get(attachment);
                    break;
                }
            }

            Path path = Paths.get(pathValue);
            Resource resource = new UrlResource(path.toUri());

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + attachment.getFileName() + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/attachments/{attId}")
    public ResponseEntity<?> deleteAttachment(
            @PathVariable Long attId,
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            TaskAttachment attachment = taskAttachmentRepository.findById(attId)
                    .orElseThrow(() -> new RuntimeException("Fișierul nu există"));

            String pathValue = "";
            for (Field field : TaskAttachment.class.getDeclaredFields()) {
                if (field.getType().equals(String.class) &&
                        (field.getName().toLowerCase().contains("path") || field.getName().toLowerCase().contains("url"))) {
                    field.setAccessible(true);
                    pathValue = (String) field.get(attachment);
                    break;
                }
            }

            try {
                Files.deleteIfExists(Paths.get(pathValue));
            } catch (Exception e) {}

            taskAttachmentRepository.delete(attachment);
            return ResponseEntity.ok().body("{\"message\": \"Șters\"}");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}
