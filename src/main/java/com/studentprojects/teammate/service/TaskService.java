package com.studentprojects.teammate.service;

import com.studentprojects.teammate.dto.CreateTaskRequest;
import com.studentprojects.teammate.dto.TaskResponse;
import com.studentprojects.teammate.dto.UpdateTaskRequest;
import com.studentprojects.teammate.entity.Task;
import com.studentprojects.teammate.entity.TaskStatus;
import com.studentprojects.teammate.entity.User;
import com.studentprojects.teammate.entity.Phase;
import com.studentprojects.teammate.repository.TaskRepository;
import com.studentprojects.teammate.repository.UserRepository;
import com.studentprojects.teammate.repository.PhaseRepository;
import com.studentprojects.teammate.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final PhaseRepository phaseRepository;
    private final ProjectRepository projectRepository;
    private final NotificationService notificationService;
    private final SprintService sprintService;

    @Transactional
    public TaskResponse createTask(Long phaseId, CreateTaskRequest request, Long userId) {
        Phase phase = phaseRepository.findById(phaseId)
                .orElseThrow(() -> new RuntimeException("Etapa nu a fost gasita"));

        Task task = Task.builder()
                .phaseId(phaseId)
                .projectId(phase.getProjectId())
                .sprintId(phase.getSprintId())
                .title(request.getTitle())
                .description(request.getDescription())
                .status(TaskStatus.TODO)
                .createdBy(userId)
                .assignedUsers(new HashSet<>())
                .build();

        if (request.getAssignedTo() != null && !request.getAssignedTo().isEmpty()) {
            List<User> usersToAssign = userRepository.findAllById(request.getAssignedTo());
            task.getAssignedUsers().addAll(usersToAssign);

            // Notificare pentru fiecare membru atribuit
            var project = projectRepository.findById(phase.getProjectId()).orElse(null);
            String projectName = project != null ? project.getName() : "proiect";
            for (User u : usersToAssign) {
                if (!u.getId().equals(userId)) {
                    notificationService.sendToUser(
                            u.getId(),
                            "Ai fost atribuit la task-ul \"" + request.getTitle() + "\" in proiectul " + projectName + ".",
                            "TASK_ASSIGNED", phase.getProjectId(), null, null
                    );
                }
            }
        }

        Task savedTask = taskRepository.save(task);
        return mapToResponse(savedTask);
    }

    @Transactional
    public TaskResponse updateTask(Long taskId, UpdateTaskRequest request, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task-ul nu a fost gasit"));

        boolean isOwnerModifying = task.getAssignedUsers().stream()
                .noneMatch(u -> u.getId().equals(userId));

        if (request.getTitle() != null) task.setTitle(request.getTitle());
        if (request.getDescription() != null) task.setDescription(request.getDescription());
        if (request.getStatus() != null) task.setStatus(request.getStatus());

        if (request.getAssignedTo() != null) {
            List<User> usersToAssign = userRepository.findAllById(request.getAssignedTo());
            task.setAssignedUsers(new HashSet<>(usersToAssign));

            // Notificare atribuire noua
            if (isOwnerModifying) {
                var project = projectRepository.findById(task.getProjectId()).orElse(null);
                String projectName = project != null ? project.getName() : "proiect";
                for (User u : usersToAssign) {
                    if (!u.getId().equals(userId)) {
                        notificationService.sendToUser(
                                u.getId(),
                                "Ai fost atribuit la task-ul \"" + task.getTitle() + "\" in proiectul " + projectName + ".",
                                "TASK_ASSIGNED", task.getProjectId(), null, null
                        );
                    }
                }
            }
        }

        // Notificare modificare de scrum + reset status la IN_PROGRESS
        if (isOwnerModifying && request.getStatus() == null) {
            // Reset status dacă era DONE
            if (task.getStatus() == TaskStatus.DONE) {
                task.setStatus(TaskStatus.IN_PROGRESS);
            }

            // Notificare către membrii atribuiți
            for (User u : task.getAssignedUsers()) {
                if (!u.getId().equals(userId)) {
                    notificationService.sendToUser(
                            u.getId(),
                            "Scrum Master-ul a facut o modificare in task-ul tau \"" + task.getTitle() + "\".",
                            "TASK_MODIFIED", task.getProjectId(), task.getId(), null
                    );
                }
            }
        }

        Task updatedTask = taskRepository.save(task);

        // Verificare sprint complet
        if (request.getStatus() == TaskStatus.DONE) {
            checkAndNotifySprintComplete(task.getPhaseId(), task.getProjectId());
        }

        return mapToResponse(updatedTask);
    }

    private void checkAndNotifySprintComplete(Long phaseId, Long projectId) {
        List<Task> allTasksInPhase = taskRepository.findByPhaseId(phaseId);
        if (allTasksInPhase.isEmpty()) return;

        boolean allDone = allTasksInPhase.stream()
                .allMatch(t -> t.getStatus() == TaskStatus.DONE);
        if (!allDone) return;

        var phase = phaseRepository.findById(phaseId).orElse(null);
        var project = projectRepository.findById(projectId).orElse(null);
        if (phase == null || project == null) return;

        Integer sprintIndex = phase.getOrderIndex();
        boolean alreadyNotified = notificationService.existsSprintDoneNotification(
                projectId, project.getCreatorId(), sprintIndex
        );
        if (!alreadyNotified) {
            sprintService.notifySprintComplete(projectId, sprintIndex.longValue());
        }
    }

    @Transactional
    public TaskResponse claimTask(Long taskId, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task-ul nu a fost gasit"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilizatorul nu a fost gasit"));
        task.getAssignedUsers().add(user);
        Task updatedTask = taskRepository.save(task);
        return mapToResponse(updatedTask);
    }

    @Transactional
    public void deleteTask(Long taskId, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task-ul nu a fost gasit"));
        taskRepository.delete(task);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getMyTasks(Long userId) {
        return taskRepository.findAll().stream()
                .filter(t -> t.getAssignedUsers().stream().anyMatch(u -> u.getId().equals(userId)))
                .map(this::mapToResponse)
                .toList();
    }

    private TaskResponse mapToResponse(Task task) {
        TaskResponse response = new TaskResponse();
        response.setId(task.getId());
        response.setPhaseId(task.getPhaseId());
        response.setProjectId(task.getProjectId());
        response.setTitle(task.getTitle());
        response.setDescription(task.getDescription());
        response.setStatus(task.getStatus());
        response.setCreatedBy(task.getCreatedBy());

        if (task.getAssignedUsers() != null) {
            List<Long> assignedIds = task.getAssignedUsers().stream()
                    .map(User::getId).toList();
            response.setAssignedTo(assignedIds);

            List<String> assignedNames = task.getAssignedUsers().stream()
                    .map(User::getName).toList();
            response.setAssignedToNames(assignedNames);
        }

        return response;
    }
}