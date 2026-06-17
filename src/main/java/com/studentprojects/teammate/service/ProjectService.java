package com.studentprojects.teammate.service;

import com.studentprojects.teammate.dto.CreateProjectRequest;
import com.studentprojects.teammate.dto.ProjectResponse;
import com.studentprojects.teammate.entity.MemberStatus;
import com.studentprojects.teammate.entity.Phase;
import com.studentprojects.teammate.entity.Project;
import com.studentprojects.teammate.entity.ProjectMember;
import com.studentprojects.teammate.entity.ProjectStatus;
import com.studentprojects.teammate.entity.User;
import com.studentprojects.teammate.repository.PhaseRepository;
import com.studentprojects.teammate.repository.ProjectMemberRepository;
import com.studentprojects.teammate.repository.ProjectRepository;
import com.studentprojects.teammate.repository.SprintRepository;
import com.studentprojects.teammate.repository.TaskRepository;
import com.studentprojects.teammate.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final PhaseRepository phaseRepository;
    private final TaskRepository taskRepository;
    private final SprintRepository sprintRepository;

    public ProjectResponse createProject(CreateProjectRequest request, Long creatorId) {
        if (projectRepository.existsByCreatorIdAndName(creatorId, request.getName())) {
            throw new RuntimeException("Ai deja un proiect cu acest nume.");
        }

        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .deadline(request.getDeadline())
                .startDate(request.getStartDate())
                .status(request.getStartDate() != null && request.getStartDate().isAfter(LocalDate.now())
                        ? ProjectStatus.NOT_STARTED
                        : ProjectStatus.ACTIVE)
                .creatorId(creatorId)
                .build();

        Project savedProject = projectRepository.save(project);

        if (request.getPhases() != null && !request.getPhases().isEmpty()) {
            int orderIndex = 1;
            List<Phase> savedPhases = new ArrayList<>();
            for (CreateProjectRequest.PhaseData phaseData : request.getPhases()) {
                Phase phase = Phase.builder()
                        .projectId(savedProject.getId())
                        .name(phaseData.getName())
                        .difficulty(phaseData.getDifficulty())
                        .orderIndex(phaseData.getOrderIndex() != null ? phaseData.getOrderIndex() : orderIndex)
                        .status(com.studentprojects.teammate.entity.PhaseStatus.NOT_STARTED)
                        .build();
                savedPhases.add(phaseRepository.save(phase));
                orderIndex++;
            }

            // ── NOU: calculează datele sprinturilor ──
            if (request.getStartDate() != null && request.getDeadline() != null) {
                int bufferDays = request.getBufferDays() != null ? request.getBufferDays() : 0;
                calculateAndSaveSprintDates(
                        savedPhases,
                        request.getStartDate(),
                        request.getDeadline(),
                        bufferDays
                );
            }
            // ─────────────────────────────────────────
        }

        return convertToResponse(savedProject);
    }

    // ── NOU: algoritm proporțional cu difficulty + buffer ────────────────────
    private void calculateAndSaveSprintDates(List<Phase> phases, LocalDate startDate,
                                             LocalDate deadline, int bufferDays) {
        // Data efectivă de sfârșit = deadline - buffer
        LocalDate effectiveEnd = deadline.minusDays(bufferDays);

        // Dacă după buffer nu mai avem zile, folosim deadline direct
        if (!effectiveEnd.isAfter(startDate)) {
            effectiveEnd = deadline;
        }

        long totalDays = java.time.temporal.ChronoUnit.DAYS.between(startDate, effectiveEnd);
        if (totalDays <= 0) return;

        // Suma totală a difficulty-urilor
        int sumDifficulty = phases.stream()
                .mapToInt(p -> p.getDifficulty() != null ? p.getDifficulty() : 1)
                .sum();

        LocalDate currentStart = startDate;
        for (int i = 0; i < phases.size(); i++) {
            Phase phase = phases.get(i);
            int diff = phase.getDifficulty() != null ? phase.getDifficulty() : 1;

            LocalDate phaseEnd;
            if (i == phases.size() - 1) {
                // Ultima fază se termină exact pe effectiveEnd
                phaseEnd = effectiveEnd;
            } else {
                long phaseDays = Math.round((double) diff / sumDifficulty * totalDays);
                // Minim 1 zi per sprint
                if (phaseDays < 1) phaseDays = 1;
                phaseEnd = currentStart.plusDays(phaseDays);
                // Nu depăși effectiveEnd
                if (phaseEnd.isAfter(effectiveEnd)) phaseEnd = effectiveEnd;
            }

            phase.setStartDate(currentStart);
            phase.setEndDate(phaseEnd);
            phaseRepository.save(phase);

            currentStart = phaseEnd;
        }
    }
    // ─────────────────────────────────────────────────────────────────────────

    public List<ProjectResponse> getUserProjects(Long userId) {
        List<Project> ownedProjects = projectRepository.findByCreatorId(userId);

        List<ProjectMember> acceptedMemberships = projectMemberRepository
                .findByUserIdAndStatus(userId, MemberStatus.ACCEPTED);

        List<Project> memberProjects = acceptedMemberships.stream()
                .map(member -> projectRepository.findById(member.getProjectId()).orElse(null))
                .filter(p -> p != null)
                .collect(Collectors.toList());

        List<Project> allProjects = new ArrayList<>();
        allProjects.addAll(ownedProjects);

        for (Project memberProject : memberProjects) {
            boolean alreadyAdded = allProjects.stream()
                    .anyMatch(p -> p.getId().equals(memberProject.getId()));
            if (!alreadyAdded) {
                allProjects.add(memberProject);
            }
        }

        return allProjects.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public ProjectResponse getProjectById(Long projectId, Long userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Proiectul nu a fost gasit."));

        boolean isCreator = project.getCreatorId().equals(userId);
        boolean isMember = projectMemberRepository
                .findByUserIdAndStatus(userId, MemberStatus.ACCEPTED)
                .stream()
                .anyMatch(m -> m.getProjectId().equals(projectId));

        if (!isCreator && !isMember) {
            throw new RuntimeException("Nu ai acces la acest proiect.");
        }

        return convertToResponse(project);
    }

    private ProjectResponse convertToResponse(Project project) {
        User creator = userRepository.findById(project.getCreatorId()).orElse(null);

        List<String> memberNames = new ArrayList<>();

        if (creator != null) {
            memberNames.add(creator.getName());
        }

        List<ProjectMember> allMembers = projectMemberRepository
                .findByProjectId(project.getId());

        for (ProjectMember member : allMembers) {
            User user = userRepository.findById(member.getUserId()).orElse(null);
            if (user != null) {
                String status = member.getStatus() == MemberStatus.ACCEPTED ? "ACCEPTED" : "PENDING";
                memberNames.add(user.getName() + "|" + status);
            }
        }

        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .deadline(project.getDeadline())
                .startDate(project.getStartDate())
                .status(project.getStatus())
                .creatorId(project.getCreatorId())
                .creatorName(creator != null ? creator.getName() : "Necunoscut")
                .creatorUsername(creator != null ? creator.getUsername() : "necunoscut")
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .members(memberNames)
                .build();
    }

    public ProjectResponse updateProject(Long projectId, CreateProjectRequest request, Long userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Proiectul nu a fost găsit."));

        if (!project.getCreatorId().equals(userId)) {
            throw new RuntimeException("Doar proprietarul poate modifica acest proiect.");
        }

        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setDeadline(request.getDeadline());
        if (request.getStartDate() != null) project.setStartDate(request.getStartDate());

        Project saved = projectRepository.save(project);

        // ── NOU: recalculează datele sprinturilor la editare ──
        if (request.getDeadline() != null) {
            List<Phase> phases = phaseRepository.findByProjectIdOrderByOrderIndexAsc(projectId);
            if (!phases.isEmpty()) {
                LocalDate start = saved.getStartDate() != null
                        ? saved.getStartDate()
                        : LocalDate.now();
                int bufferDays = request.getBufferDays() != null ? request.getBufferDays() : 0;
                calculateAndSaveSprintDates(phases, start, request.getDeadline(), bufferDays);
            }
        }
        // ─────────────────────────────────────────────────────

        return convertToResponse(saved);
    }

    public void deleteProject(Long projectId, Long userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Proiectul nu a fost găsit."));

        if (!project.getCreatorId().equals(userId)) {
            throw new RuntimeException("Doar proprietarul poate șterge acest proiect.");
        }
        sprintRepository.deleteByProjectId(projectId);
        phaseRepository.deleteByProjectId(projectId);
        taskRepository.deleteByProjectId(projectId);
        projectMemberRepository.deleteByProjectId(projectId);
        projectRepository.deleteById(projectId);
    }
    public ProjectResponse archiveProject(Long projectId, Long userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Proiectul nu a fost găsit."));
        if (!project.getCreatorId().equals(userId)) {
            throw new RuntimeException("Doar proprietarul poate arhiva acest proiect.");
        }
        if (project.getStatus() != ProjectStatus.COMPLETED) {
            throw new RuntimeException("Doar proiectele finalizate pot fi arhivate.");
        }
        project.setStatus(ProjectStatus.ARCHIVED);
        return convertToResponse(projectRepository.save(project));
    }

    public ProjectResponse unarchiveProject(Long projectId, Long userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Proiectul nu a fost găsit."));
        if (!project.getCreatorId().equals(userId)) {
            throw new RuntimeException("Doar proprietarul poate dezarhiva acest proiect.");
        }
        if (project.getStatus() != ProjectStatus.ARCHIVED) {
            throw new RuntimeException("Proiectul nu este arhivat.");
        }
        project.setStatus(ProjectStatus.COMPLETED);
        return convertToResponse(projectRepository.save(project));
    }
}