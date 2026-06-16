package com.studentprojects.teammate.controller;

import com.studentprojects.teammate.dto.AuthResponse;
import com.studentprojects.teammate.dto.LoginRequest;
import com.studentprojects.teammate.dto.RegisterRequest;
import com.studentprojects.teammate.entity.User;
import com.studentprojects.teammate.repository.UserRepository;
import com.studentprojects.teammate.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.RequestMethod;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173", allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
                RequestMethod.DELETE, RequestMethod.OPTIONS})
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    // Clasă internă pentru răspuns eroare
    record ErrorResponse(String error) {}

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }
    @GetMapping("/search-users")
    public ResponseEntity<?> searchUsers(@RequestParam String q, HttpServletRequest request) {
        if (q == null || q.trim().length() < 2) {
            return ResponseEntity.ok(List.of());
        }
        List<User> users = userRepository.findByUsernameContainingIgnoreCaseOrNameContainingIgnoreCase(
                q.trim(), q.trim()
        );
        List<Map<String, String>> result = users.stream()
                .map(u -> Map.of(
                        "username", u.getUsername(),
                        "name", u.getName()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

}