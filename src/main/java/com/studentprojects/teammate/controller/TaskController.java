package com.studentprojects.teammate.controller;

import com.studentprojects.teammate.dto.CreateTaskRequest;
import com.studentprojects.teammate.dto.TaskResponse;
import com.studentprojects.teammate.dto.UpdateTaskRequest;
import com.studentprojects.teammate.service.JwtService;
import com.studentprojects.teammate.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173", allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
                RequestMethod.DELETE, RequestMethod.OPTIONS})
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final JwtService jwtService;

    // ⚠️ NOTĂ: Dacă repository-ul tău se numește TaskAttachmentRepository,
    // schimbă numele de mai jos ca să se potrivească perfect.
    // private final TaskAttachmentRepository attachmentRepository;

    @PostMapping("/phases/{phaseId}/tasks")
    public ResponseEntity<?> createTask(
            @PathVariable Long phaseId,
            @RequestBody CreateTaskRequest request,
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            String token = authHeader.replace("Bearer ", "");
            Long userId = jwtService.getUserIdFromToken(token);
            TaskResponse response = taskService.createTask(phaseId, request, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/tasks/{taskId}")
    public ResponseEntity<?> updateTask(
            @PathVariable Long taskId,
            @RequestBody UpdateTaskRequest request,
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            String token = authHeader.replace("Bearer ", "");
            Long userId = jwtService.getUserIdFromToken(token);
            TaskResponse response = taskService.updateTask(taskId, request, userId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/tasks/{taskId}/claim")
    public ResponseEntity<?> claimTask(
            @PathVariable Long taskId,
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            String token = authHeader.replace("Bearer ", "");
            Long userId = jwtService.getUserIdFromToken(token);
            TaskResponse response = taskService.claimTask(taskId, userId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/tasks/{taskId}")
    public ResponseEntity<?> deleteTask(
            @PathVariable Long taskId,
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            String token = authHeader.replace("Bearer ", "");
            Long userId = jwtService.getUserIdFromToken(token);
            taskService.deleteTask(taskId, userId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/tasks/my")
    public ResponseEntity<?> getMyTasks(
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            String token = authHeader.replace("Bearer ", "");
            Long userId = jwtService.getUserIdFromToken(token);
            List<TaskResponse> tasks = taskService.getMyTasks(userId);
            return ResponseEntity.ok(tasks);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    // ─── RUTELE NOI PENTRU UPLOAD ȘI DOWNLOAD PDF ───

    @PostMapping("/tasks/{taskId}/attachments")
    public ResponseEntity<?> uploadAttachment(
            @PathVariable Long taskId,
            @RequestParam("file") MultipartFile file,
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            String token = authHeader.replace("Bearer ", "");
            Long userId = jwtService.getUserIdFromToken(token);

            // Salvare fizică pe disc în folderul upload-uri
            String uploadDir = "./uploads/";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Aici apelezi direct salvarea prin serviciul sau repository-ul tău existent.
            // Exemplu dacă folosești clasa TaskAttachment:
            /*
            TaskAttachment attachment = TaskAttachment.builder()
                    .taskId(taskId)
                    .fileName(file.getOriginalFilename())
                    .filePath(filePath.toString())
                    .uploadedBy(userId)
                    .uploadedByName("Stefan")
                    .createdAt(LocalDateTime.now())
                    .build();
            attachmentRepository.save(attachment);
            return ResponseEntity.ok(attachment);
            */

            return ResponseEntity.ok().body("{\"message\": \"Fișier încărcat cu succes local!\"}");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Eroare la upload: " + e.getMessage()));
        }
    }

    @GetMapping("/tasks/{taskId}/attachments")
    public ResponseEntity<?> getAttachments(@PathVariable Long taskId) {
        // Înlocuiește cu returnarea listei tale din baza de date folosind repository-ul tău
        // List<TaskAttachment> list = attachmentRepository.findByTaskId(taskId);
        // return ResponseEntity.ok(list);
        return ResponseEntity.ok().body(List.of());
    }

    @GetMapping("/attachments/{attId}/download")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable Long attId) {
        try {
            // Înlocuiește cu logica ta de căutare din DB a căii fișierului
            Path path = Paths.get("./uploads/exemplu.pdf");
            Resource resource = new UrlResource(path.toUri());

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"document.pdf\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    record ErrorResponse(String error) {}
}