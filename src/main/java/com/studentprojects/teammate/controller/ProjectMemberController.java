package com.studentprojects.teammate.controller;

import com.studentprojects.teammate.dto.InviteRequest;
import com.studentprojects.teammate.dto.MemberResponse;
import com.studentprojects.teammate.service.JwtService;
import com.studentprojects.teammate.service.ProjectMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class ProjectMemberController {

    private final ProjectMemberService projectMemberService;
    private final JwtService jwtService;

    // Invita un user in proiect
    @PostMapping("/projects/{projectId}/invite")
    public ResponseEntity<?> inviteUser(
            @PathVariable Long projectId,
            @Valid @RequestBody InviteRequest request,
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            String token = authHeader.replace("Bearer ", "");
            Long inviterId = jwtService.getUserIdFromToken(token);

            MemberResponse response = projectMemberService.inviteUser(projectId, request, inviterId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    // Listeaza membrii unui proiect
    @GetMapping("/projects/{projectId}/members")
    public ResponseEntity<?> getProjectMembers(
            @PathVariable Long projectId,
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            String token = authHeader.replace("Bearer ", "");
            Long userId = jwtService.getUserIdFromToken(token);

            List<MemberResponse> members = projectMemberService.getProjectMembers(projectId, userId);
            return ResponseEntity.ok(members);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    // Listeaza invitatiile mele PENDING (pentru plic)
    @GetMapping("/invitations/pending")
    public ResponseEntity<?> getMyPendingInvitations(
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            String token = authHeader.replace("Bearer ", "");
            Long userId = jwtService.getUserIdFromToken(token);

            List<MemberResponse> invitations = projectMemberService.getMyPendingInvitations(userId);
            return ResponseEntity.ok(invitations);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    // Accepta o invitatie
    @PostMapping("/invitations/{invitationId}/accept")
    public ResponseEntity<?> acceptInvitation(
            @PathVariable Long invitationId,
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            String token = authHeader.replace("Bearer ", "");
            Long userId = jwtService.getUserIdFromToken(token);

            MemberResponse response = projectMemberService.acceptInvitation(invitationId, userId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    // Respinge o invitatie
    @PostMapping("/invitations/{invitationId}/reject")
    public ResponseEntity<?> rejectInvitation(
            @PathVariable Long invitationId,
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            String token = authHeader.replace("Bearer ", "");
            Long userId = jwtService.getUserIdFromToken(token);

            MemberResponse response = projectMemberService.rejectInvitation(invitationId, userId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    record ErrorResponse(String error) {}
}
