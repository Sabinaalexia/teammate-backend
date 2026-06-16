package com.studentprojects.teammate.service;

import com.studentprojects.teammate.dto.AuthResponse;
import com.studentprojects.teammate.dto.RegisterRequest;
import com.studentprojects.teammate.dto.LoginRequest;
import com.studentprojects.teammate.entity.User;
import com.studentprojects.teammate.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest request) {
        // 1. Verifică dacă email-ul există deja
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email-ul deja există!");
        }

        // 2. Verifică dacă username-ul există deja
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username-ul deja există!");
        }

        // 3. Creează user nou
        User user = new User();
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // Criptează parola
        user.setName(request.getName());

        // 4. Salvează în baza de date
        User savedUser = userRepository.save(user);



                // 6. Generează JWT token
                String token = jwtService.generateToken(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getUsername()
        );

        // 7. Returnează răspuns cu token
        return new AuthResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getUsername(),
                savedUser.getName(),
                token,
                "Account created successfully!"

        );
    }
    public AuthResponse login(LoginRequest request) {
        // 1. Găsește user după email SAU username
        User user = userRepository.findByEmail(request.getEmailOrUsername())
                .orElseGet(() -> userRepository.findByUsername(request.getEmailOrUsername())
                        .orElseThrow(() -> new RuntimeException("Email sau username incorect")));

        // 2. Verifică parola
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Parolă incorectă");
        }
        String token = jwtService.generateToken(
                user.getId(),
                user.getEmail(),
                user.getUsername()
        );

        // 3. Returnează răspuns (user autentificat)
        return new AuthResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getName(),
                token,
                "Autentificare reușită!"
        );
    }
}