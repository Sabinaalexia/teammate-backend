package com.studentprojects.teammate.service;

import com.studentprojects.teammate.entity.*;
import com.studentprojects.teammate.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.studentprojects.teammate.entity.Project;
import com.studentprojects.teammate.entity.ProjectStatus;
import com.studentprojects.teammate.repository.ProjectRepository;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SprintDeadlineScheduler {

    private final PhaseRepository phaseRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final NotificationService notificationService;


    @Scheduled(cron = "0 0 17 * * *") // rulează în fiecare zi la 16:00
    public void checkSprintDeadlines() {
        LocalDate azi = LocalDate.now();
        List<Phase> activePhases = phaseRepository.findAll().stream()
                .filter(p -> !p.isConfirmedByScrum() && p.getEndDate() != null)
                .toList();

        for (Phase phase : activePhases) {
            long zileRamase = ChronoUnit.DAYS.between(azi, phase.getEndDate());

            if (zileRamase == 5 || zileRamase == 3 || zileRamase == 1) {
                var project = projectRepository.findById(phase.getProjectId()).orElse(null);
                if (project == null) continue;

                String zileCuvant = zileRamase == 1 ? "o zi" : zileRamase + " zile";

                // Mesaj pentru membri
                String memberMsg = "Mai sunt " + zileCuvant + " până se termină Sprintul " +
                        phase.getOrderIndex() + " din proiectul \"" + project.getName() +
                        "\". Asigură-te că ești aproape să îți finalizezi toate taskurile atribuite.";

                // Mesaj pentru scrum master
                String scrumMsg = "Mai sunt " + zileCuvant + " până se termină Sprintul " +
                        phase.getOrderIndex() + " din proiectul \"" + project.getName() +
                        "\". Asigură-te că membrii echipei sunt aproape să își finalizeze toate taskurile atribuite.";

                // Notificare scrum master
                notificationService.sendToUser(
                        project.getCreatorId(), scrumMsg, "SPRINT_DEADLINE",
                        phase.getProjectId(), null, phase.getOrderIndex()
                );

                // Notificări membri
                projectMemberRepository.findByProjectId(phase.getProjectId()).stream()
                        .filter(m -> m.getStatus() == MemberStatus.ACCEPTED)
                        .filter(m -> !m.getUserId().equals(project.getCreatorId()))
                        .forEach(m -> notificationService.sendToUser(
                                m.getUserId(), memberMsg, "SPRINT_DEADLINE",
                                phase.getProjectId(), null, phase.getOrderIndex()
                        ));
            }
        }
    }
    @Scheduled(cron = "0 1 0 * * *") // rulează în fiecare zi la 00:01
    public void activateProjects() {
        LocalDate azi = LocalDate.now();

        projectRepository.findAll().stream()
                .filter(p -> p.getStatus() == ProjectStatus.NOT_STARTED)
                .filter(p -> p.getStartDate() != null && !p.getStartDate().isAfter(azi))
                .forEach(p -> {
                    p.setStatus(ProjectStatus.ACTIVE);
                    projectRepository.save(p);
                });
    }
}