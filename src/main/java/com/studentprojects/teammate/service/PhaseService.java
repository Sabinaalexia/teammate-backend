package com.studentprojects.teammate.service;

import com.studentprojects.teammate.dto.PhaseResponse;
import com.studentprojects.teammate.dto.TaskResponse;
import com.studentprojects.teammate.entity.*;
import com.studentprojects.teammate.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PhaseService {

    private final PhaseRepository phaseRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final NotificationService notificationService;
    private final TaskAssignmentRepository taskAssignmentRepository;

    public List<PhaseResponse> getProjectPhases(Long projectId) {
        List<Phase> phases = phaseRepository.findByProjectIdOrderByOrderIndexAsc(projectId);
        return phases.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    // Scrumul confirmă că sprintul curent e gata → trece la următorul
    @Transactional
    public PhaseResponse confirmPhase(Long phaseId, Long userId) {
        Phase phase = phaseRepository.findById(phaseId)
                .orElseThrow(() -> new RuntimeException("Etapa nu a fost găsită."));

        var project = projectRepository.findById(phase.getProjectId())
                .orElseThrow(() -> new RuntimeException("Proiectul nu a fost găsit."));

        if (!project.getCreatorId().equals(userId)) {
            throw new RuntimeException("Doar Scrum Master-ul poate confirma etapa.");
        }

        phase.setConfirmedByScrum(true);

        // ── Redistribuire zile rămase la sprint-ul următor ──
        LocalDate azi = LocalDate.now();
        LocalDate endPlanificat = phase.getEndDate();
        List<Phase> allPhases = phaseRepository.findByProjectIdOrderByOrderIndexAsc(phase.getProjectId());
        if (endPlanificat != null && azi.isBefore(endPlanificat)) {
            long zileRamase = java.time.temporal.ChronoUnit.DAYS.between(azi, endPlanificat);
            allPhases.stream()
                    .filter(p -> p.getOrderIndex() != null && p.getOrderIndex() == phase.getOrderIndex() + 1)
                    .findFirst()
                    .ifPresent(nextPhase -> {
                        if (nextPhase.getEndDate() != null) {
                            nextPhase.setStartDate(azi);
                            nextPhase.setEndDate(nextPhase.getEndDate().plusDays(zileRamase));
                            phaseRepository.save(nextPhase);
                        }
                    });
            phase.setEndDate(azi);
        }
// ────────────────────────────────────────────────────
        phase.setStatus(PhaseStatus.COMPLETED);
        phaseRepository.save(phase);

        // Notificare scrum — felicitări
        String scrumMsg;
        // Verifică dacă e ultimul sprint

        boolean isLastPhase = allPhases.stream()
                .filter(p -> !p.getId().equals(phaseId))
                .allMatch(Phase::isConfirmedByScrum);

        if (isLastPhase) {
            scrumMsg = "Felicitări! Proiectul \"" + project.getName() + "\" a fost finalizat cu succes! Ați reușit!";
            // ── NOU: setează proiectul ca finalizat ──
            project.setStatus(ProjectStatus.COMPLETED);
            projectRepository.save(project);
            // ─────────────────────────────────────────
        } else {
            // Găsim numărul următorului sprint
            int nextSprint = phase.getOrderIndex() + 1;
            scrumMsg = "Felicitări! Ați mai făcut un pas. Acum adaugă noile taskuri în Sprintul " + nextSprint + " și atribuie-le colegilor tăi și hai la treabă!";

        }

        notificationService.sendToUser(userId, scrumMsg, "SPRINT_CONFIRMED",
                phase.getProjectId(), null, phase.getOrderIndex());

        // Notificare membri — sprintul s-a încheiat
        String memberMsg = isLastPhase
                ? "Felicitări! Proiectul \"" + project.getName() + "\" a fost finalizat cu succes! Ați reușit!"
                : "Toată lumea și-a terminat taskurile din Sprintul " + phase.getOrderIndex() + ", prin urmare sprintul s-a încheiat cu succes. " +
                "Fii pe fază, începe Sprintul " + (phase.getOrderIndex() + 1) + "! Vezi ce task ți-a fost atribuit și hai la treabă!";


        String memberType = isLastPhase ? "SPRINT_CONFIRMED" : "SPRINT_DONE_MEMBER";

        projectMemberRepository.findByProjectId(phase.getProjectId()).stream()
                .filter(m -> m.getStatus() == MemberStatus.ACCEPTED)
                .filter(m -> !m.getUserId().equals(userId))
                .forEach(m -> notificationService.sendToUser(
                        m.getUserId(), memberMsg, memberType,
                        phase.getProjectId(), null, phase.getOrderIndex()));

        return convertToResponse(phase);
    }

    // Notifică scrumul când toți membrii termină taskurile
    @Transactional
    public void notifyAllTasksDone(Long phaseId) {
        Phase phase = phaseRepository.findById(phaseId)
                .orElseThrow(() -> new RuntimeException("Etapa nu a fost găsită."));

        // Verifică dacă toate taskurile sunt done
        List<Task> tasks = taskRepository.findByPhaseIdOrderByCreatedAtAsc(phaseId);
        if (tasks.isEmpty()) return;
        boolean allDone = tasks.stream().allMatch(t -> t.getStatus() == TaskStatus.DONE);
        if (!allDone) return;

        var project = projectRepository.findById(phase.getProjectId())
                .orElseThrow(() -> new RuntimeException("Proiectul nu a fost găsit."));

        String scrumMsg = "Toată lumea și-a terminat taskurile din Sprintul " + phase.getOrderIndex() + ", prin urmare sprintul s-a încheiat cu succes. " +
                "Fii pe fază și confirmă dacă totul este conform cerințelor sau vrei să propui modificări!";

        notificationService.sendToUser(project.getCreatorId(), scrumMsg, "SPRINT_DONE_SCRUM",
                phase.getProjectId(), null, phase.getOrderIndex());
    }

    public PhaseResponse convertToResponse(Phase phase) {
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
                .confirmedByScrum(phase.isConfirmedByScrum())
                .tasks(taskResponses)
                .startDate(phase.getStartDate())
                .endDate(phase.getEndDate())
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