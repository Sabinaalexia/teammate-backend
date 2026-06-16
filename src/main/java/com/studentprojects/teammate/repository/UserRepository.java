package com.studentprojects.teammate.repository;

import com.studentprojects.teammate.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);
    List<User> findByUsernameContainingIgnoreCaseOrNameContainingIgnoreCase(String username, String name);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);
}