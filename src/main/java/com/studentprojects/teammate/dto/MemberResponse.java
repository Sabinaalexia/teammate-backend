package com.studentprojects.teammate.dto;

import com.studentprojects.teammate.entity.MemberStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class MemberResponse {

    private Long id;
    private Long projectId;
    private String projectName;

    // Info user invitat
    private Long userId;
    private String userName;
    private String userUsername;
    private String userEmail;

    // Info despre cine a invitat
    private Long invitedBy;
    private String invitedByName;

    private MemberStatus status;
    private LocalDateTime invitedAt;
    private LocalDateTime respondedAt;
}
