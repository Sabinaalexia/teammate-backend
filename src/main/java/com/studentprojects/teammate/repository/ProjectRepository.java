package com.studentprojects.teammate.repository;

import com.studentprojects.teammate.entity.Project;
import com.studentprojects.teammate.entity.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    // Găsește toate proiectele create de un user
    List<Project> findByCreatorId(Long creatorId);

    // Găsește proiectele unui user după status
    List<Project> findByCreatorIdAndStatus(Long creatorId, ProjectStatus status);

    // Verifică dacă un user are deja un proiect cu același nume
    boolean existsByCreatorIdAndName(Long creatorId, String name);

}