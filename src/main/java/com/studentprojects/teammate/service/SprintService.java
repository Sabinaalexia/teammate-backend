package com.studentprojects.teammate.service;

import com.studentprojects.teammate.dto.*;
import com.studentprojects.teammate.entity.*;
import com.studentprojects.teammate.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SprintService {

    private final SprintRepository sprintRepository;
    private final PhaseRepository phaseRepository;
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final NotificationService notificationService;
    private final TaskAssignmentRepository taskAssignmentRepository;
    private String createdByName;

    public SprintResponse createSprint(Long projectId, CreateSprintRequest request, Long userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Proiectul nu a fost găsit."));

        if (!project.getCreatorId().equals(userId)) {
            throw new RuntimeException("Doar Scrum Master-ul poate crea sprint-uri.");
        }

        sprintRepository.findByProjectIdAndStatus(projectId, SprintStatus.ACTIVE)
                .ifPresent(s -> { throw new RuntimeException("Există deja un sprint activ."); });

        int orderIndex = sprintRepository.findByProjectIdOrderByOrderIndexAsc(projectId).size() + 1;

        Sprint sprint = Sprint.builder()
                .projectId(projectId)
                .name(request.getName() != null ? request.getName() : "Sprint " + orderIndex)
                .orderIndex(orderIndex)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(SprintStatus.ACTIVE)
                .build();

        Sprint saved = sprintRepository.save(sprint);

        if (request.getPhaseIds() != null) {
            for (Long phaseId : request.getPhaseIds()) {
                phaseRepository.findById(phaseId).ifPresent(phase -> {
                    phase.setSprintId(saved.getId());
                    phaseRepository.save(phase);
                });
            }
        }

        return convertToResponse(saved);
    }

    public List<SprintResponse> getProjectSprints(Long projectId) {
        return sprintRepository.findByProjectIdOrderByOrderIndexAsc(projectId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public SprintResponse getActiveSprint(Long projectId) {
        Sprint sprint = sprintRepository.findByProjectIdAndStatus(projectId, SprintStatus.ACTIVE)
                .orElseThrow(() -> new RuntimeException("Nu există un sprint activ."));
        return convertToResponse(sprint);
    }

    // Confirmare sprint de către Scrum Master
    public SprintResponse confirmSprint(Long sprintId, Long userId) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new RuntimeException("Sprint-ul nu a fost găsit."));

        Project project = projectRepository.findById(sprint.getProjectId())
                .orElseThrow(() -> new RuntimeException("Proiectul nu a fost găsit."));

        if (!project.getCreatorId().equals(userId)) {
            throw new RuntimeException("Doar Scrum Master-ul poate confirma sprint-ul.");
        }

        sprint.setStatus(SprintStatus.COMPLETED);
        sprint.setConfirmedAt(LocalDateTime.now());
        Sprint saved = sprintRepository.save(sprint);

        // Notificare Scrum Master — felicitări
        notificationService.sendToUser(
                userId,
                "Felicitări! Ați mai făcut un pas. Acum adaugă noile taskuri și atribuie-le colegilor tăi și hai la treabă!",
                "SPRINT_CONFIRMED",
                project.getId(),
                null,
                sprint.getOrderIndex()
        );

        // Notificare membri — sprintul s-a încheiat
        String memberMsg = "Toată lumea și-a terminat taskurile, prin urmare sprintul s-a încheiat cu succes. " +
                "Fii pe fază, începe următorul sprint! Vezi ce task ți-a fost atribuit și hai la treabă!";
        notificationService.sendToProjectMembers(
                project.getId(),
                userId, // excludem scrumul
                memberMsg,
                "SPRINT_DONE_MEMBER",
                sprint.getOrderIndex()
        );

        return convertToResponse(saved);
    }

    // Notificare Scrum Master când toți membrii termină
    public void notifySprintComplete(Long projectId, Long sprintIndex) {
        Project project = projectRepository.findById(projectId).orElse(null);
        if (project == null) return;

        // Verifică din nou în DB chiar înainte de a trimite
        boolean alreadySent = notificationService.existsSprintDoneNotification(
                projectId, project.getCreatorId(), sprintIndex.intValue()
        );
        if (alreadySent) return;

        String scrumMsg = "Toată lumea și-a terminat taskurile, prin urmare sprintul s-a încheiat cu succes. " +
                "Fii pe fază și confirmă dacă totul este conform cerințelor sau vrei să propui modificări!";
        notificationService.sendToUser(
                project.getCreatorId(),
                scrumMsg,
                "SPRINT_DONE_SCRUM",
                projectId,
                null,
                sprintIndex.intValue()
        );

    }

    public boolean checkAllTasksDone(Long sprintId) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new RuntimeException("Sprint-ul nu a fost găsit."));

        List<Phase> phases = phaseRepository.findBySprintId(sprintId);
        if (phases.isEmpty()) return false;

        for (Phase phase : phases) {
            List<Task> tasks = taskRepository.findByPhaseIdOrderByCreatedAtAsc(phase.getId());
            if (tasks.isEmpty()) return false;
            boolean allDone = tasks.stream().allMatch(t -> t.getStatus() == TaskStatus.DONE);
            if (!allDone) return false;
        }

        sprint.setStatus(SprintStatus.PENDING_CONFIRMATION);
        sprintRepository.save(sprint);
        return true;
    }

    private SprintResponse convertToResponse(Sprint sprint) {
        List<Phase> phases = phaseRepository.findBySprintId(sprint.getId());
        List<PhaseResponse> phaseResponses = phases.stream()
                .map(phase -> {
                    List<Task> tasks = taskRepository.findByPhaseIdOrderByCreatedAtAsc(phase.getId());
                    List<TaskResponse> taskResponses = tasks.stream()
                            .map(this::convertTaskToResponse)
                            .collect(Collectors.toList());
                    return PhaseResponse.builder()
                            .id(phase.getId())
                            .projectId(phase.getProjectId())
                            .name(phase.getName())
                            .difficulty(phase.getDifficulty())
                            .status(phase.getStatus())
                            .orderIndex(phase.getOrderIndex())
                            .tasks(taskResponses)
                            .build();
                })
                .collect(Collectors.toList());

        List<Task> allTasks = phases.stream()
                .flatMap(p -> taskRepository.findByPhaseIdOrderByCreatedAtAsc(p.getId()).stream())
                .collect(Collectors.toList());

        int total = allTasks.size();
        int done = (int) allTasks.stream().filter(t -> t.getStatus() == TaskStatus.DONE).count();
        int progressPercent = total > 0 ? (done * 100 / total) : 0;
        boolean allDone = total > 0 && done == total;

        return SprintResponse.builder()
                .id(sprint.getId())
                .projectId(sprint.getProjectId())
                .name(sprint.getName())
                .orderIndex(sprint.getOrderIndex())
                .startDate(sprint.getStartDate())
                .endDate(sprint.getEndDate())
                .status(sprint.getStatus())
                .confirmedAt(sprint.getConfirmedAt())
                .phases(phaseResponses)
                .progressPercent(progressPercent)
                .allTasksDone(allDone)
                .build();
    }

    private TaskResponse convertTaskToResponse(Task task) {
        List<TaskAssignment> assignments = taskAssignmentRepository.findByTaskId(task.getId());
        List<Long> assignedIds = assignments.stream()
                .map(TaskAssignment::getUserId)
                .collect(Collectors.toList());
        List<String> assignedNames = assignments.stream()
                .map(a -> userRepository.findById(a.getUserId())
                        .map(u -> u.getName()).orElse(""))
                .collect(Collectors.toList());

        User createdByUser = userRepository.findById(task.getCreatedBy()).orElse(null);

        return TaskResponse.builder()
                .id(task.getId())
                .phaseId(task.getPhaseId())
                .projectId(task.getProjectId())
                .title(task.getTitle())
                .description(task.getDescription())
                .assignedTo(assignedIds)
                .assignedToNames(assignedNames)
                .status(task.getStatus())
                .createdBy(task.getCreatedBy())
                .createdByName(createdByUser != null ? createdByUser.getName() : null)
                .createdAt(task.getCreatedAt())
                .build();
    }
}