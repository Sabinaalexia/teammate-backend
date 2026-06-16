package com.studentprojects.teammate.service;

import com.studentprojects.teammate.dto.InviteRequest;
import com.studentprojects.teammate.dto.MemberResponse;
import com.studentprojects.teammate.entity.MemberStatus;
import com.studentprojects.teammate.entity.Project;
import com.studentprojects.teammate.entity.ProjectMember;
import com.studentprojects.teammate.entity.User;
import com.studentprojects.teammate.repository.ProjectMemberRepository;
import com.studentprojects.teammate.repository.ProjectRepository;
import com.studentprojects.teammate.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectMemberService {

    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    // Invita un user in proiect
    public MemberResponse inviteUser(Long projectId, InviteRequest request, Long inviterId) {
        // 1. Verifica daca proiectul exista
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Proiectul nu exista"));

        // 2. Verifica daca cel care invita e creatorul proiectului
        if (!project.getCreatorId().equals(inviterId)) {
            throw new RuntimeException("Doar creatorul proiectului poate invita membri");
        }

        // 3. Cauta user-ul invitat dupa email SAU username
        User invitedUser = userRepository.findByEmail(request.getEmailOrUsername())
                .orElseGet(() -> userRepository.findByUsername(request.getEmailOrUsername())
                        .orElseThrow(() -> new RuntimeException("Acest user nu exista in aplicatie")));

        // 4. Verifica ca nu se invita pe el insusi
        if (invitedUser.getId().equals(inviterId)) {
            throw new RuntimeException("Nu te poti invita pe tine in propriul proiect");
        }

        // 5. Verifica daca user-ul e deja invitat in acest proiect
        if (projectMemberRepository.existsByProjectIdAndUserId(projectId, invitedUser.getId())) {
            throw new RuntimeException("Acest user este deja invitat in proiect");
        }

        // 6. Creeaza invitatia
        ProjectMember member = new ProjectMember();
        member.setProjectId(projectId);
        member.setUserId(invitedUser.getId());
        member.setInvitedBy(inviterId);
        member.setStatus(MemberStatus.PENDING);

        ProjectMember savedMember = projectMemberRepository.save(member);

        // 7. Returneaza response cu toate detaliile
        User inviter = userRepository.findById(inviterId).orElseThrow();
        return toMemberResponse(savedMember, invitedUser, inviter, project);
    }

    // Listeaza membrii unui proiect
    public List<MemberResponse> getProjectMembers(Long projectId, Long userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Proiectul nu exista"));

        // Verifica ca user-ul are acces la proiect (e creator sau membru acceptat)
        boolean isCreator = project.getCreatorId().equals(userId);
        boolean isMember = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .map(m -> m.getStatus() == MemberStatus.ACCEPTED)
                .orElse(false);

        if (!isCreator && !isMember) {
            throw new RuntimeException("Nu ai acces la acest proiect");
        }

        List<ProjectMember> members = projectMemberRepository.findByProjectId(projectId);

        return members.stream()
                .map(member -> {
                    User invitedUser = userRepository.findById(member.getUserId()).orElseThrow();
                    User inviter = userRepository.findById(member.getInvitedBy()).orElseThrow();
                    return toMemberResponse(member, invitedUser, inviter, project);
                })
                .collect(Collectors.toList());
    }

    // Listeaza invitatiile PENDING ale unui user (pentru plicul din navbar)
    public List<MemberResponse> getMyPendingInvitations(Long userId) {
        List<ProjectMember> invitations = projectMemberRepository
                .findByUserIdAndStatus(userId, MemberStatus.PENDING);

        return invitations.stream()
                .map(invitation -> {
                    User invitedUser = userRepository.findById(invitation.getUserId()).orElseThrow();
                    User inviter = userRepository.findById(invitation.getInvitedBy()).orElseThrow();
                    Project project = projectRepository.findById(invitation.getProjectId()).orElseThrow();
                    return toMemberResponse(invitation, invitedUser, inviter, project);
                })
                .collect(Collectors.toList());
    }

    // Accepta o invitatie
    public MemberResponse acceptInvitation(Long invitationId, Long userId) {
        ProjectMember invitation = projectMemberRepository.findById(invitationId)
                .orElseThrow(() -> new RuntimeException("Invitatia nu exista"));

        // Verifica ca invitatia e pentru user-ul curent
        if (!invitation.getUserId().equals(userId)) {
            throw new RuntimeException("Aceasta invitatie nu este pentru tine");
        }

        // Verifica ca invitatia e PENDING
        if (invitation.getStatus() != MemberStatus.PENDING) {
            throw new RuntimeException("Aceasta invitatie a fost deja procesata");
        }

        invitation.setStatus(MemberStatus.ACCEPTED);
        invitation.setRespondedAt(LocalDateTime.now());

        ProjectMember saved = projectMemberRepository.save(invitation);

        User invitedUser = userRepository.findById(saved.getUserId()).orElseThrow();
        User inviter = userRepository.findById(saved.getInvitedBy()).orElseThrow();
        Project project = projectRepository.findById(saved.getProjectId()).orElseThrow();

        return toMemberResponse(saved, invitedUser, inviter, project);
    }

    // Respinge o invitatie
    public MemberResponse rejectInvitation(Long invitationId, Long userId) {
        ProjectMember invitation = projectMemberRepository.findById(invitationId)
                .orElseThrow(() -> new RuntimeException("Invitatia nu exista"));

        if (!invitation.getUserId().equals(userId)) {
            throw new RuntimeException("Aceasta invitatie nu este pentru tine");
        }

        if (invitation.getStatus() != MemberStatus.PENDING) {
            throw new RuntimeException("Aceasta invitatie a fost deja procesata");
        }

        invitation.setStatus(MemberStatus.REJECTED);
        invitation.setRespondedAt(LocalDateTime.now());

        ProjectMember saved = projectMemberRepository.save(invitation);

        User invitedUser = userRepository.findById(saved.getUserId()).orElseThrow();
        User inviter = userRepository.findById(saved.getInvitedBy()).orElseThrow();
        Project project = projectRepository.findById(saved.getProjectId()).orElseThrow();

        return toMemberResponse(saved, invitedUser, inviter, project);
    }

    // Helper: convertire ProjectMember -> MemberResponse
    private MemberResponse toMemberResponse(ProjectMember member, User invitedUser, User inviter, Project project) {
        return new MemberResponse(
                member.getId(),
                member.getProjectId(),
                project.getName(),
                invitedUser.getId(),
                invitedUser.getName(),
                invitedUser.getUsername(),
                invitedUser.getEmail(),
                inviter.getId(),
                inviter.getName(),
                member.getStatus(),
                member.getInvitedAt(),
                member.getRespondedAt()
        );
    }
}
