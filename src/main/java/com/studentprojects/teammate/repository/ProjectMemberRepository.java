package com.studentprojects.teammate.repository;

import com.studentprojects.teammate.entity.MemberStatus;
import com.studentprojects.teammate.entity.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {

    // Gaseste toti membrii unui proiect
    List<ProjectMember> findByProjectId(Long projectId);

    // Gaseste invitatiile unui user dupa status
    List<ProjectMember> findByUserIdAndStatus(Long userId, MemberStatus status);

    // Gaseste toate invitatiile unui user
    List<ProjectMember> findByUserId(Long userId);

    // Verifica daca user-ul e deja invitat in proiect
    Optional<ProjectMember> findByProjectIdAndUserId(Long projectId, Long userId);

    // Verifica daca exista deja o invitatie
    boolean existsByProjectIdAndUserId(Long projectId, Long userId);
    @Modifying
    @Transactional
    @Query("DELETE FROM ProjectMember pm WHERE pm.projectId = :projectId")
    void deleteByProjectId(@Param("projectId") Long projectId);
}
