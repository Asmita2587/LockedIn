package com.lockedin.backend.service;

import com.lockedin.backend.dto.AuthResponse;
import com.lockedin.backend.dto.LoginRequest;
import com.lockedin.backend.dto.RegisterRequest;
import com.lockedin.backend.entity.User;
import com.lockedin.backend.repository.UserRepository;
import com.lockedin.backend.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final CreditService creditService;
    private final String allowedEmailDomain;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil,
            CreditService creditService,
            @Value("${lockedin.signup.allowed-email-domain:}") String allowedEmailDomain
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.creditService = creditService;
        this.allowedEmailDomain = allowedEmailDomain == null ? "" : allowedEmailDomain.trim();
    }

    public AuthResponse register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        if (!allowedEmailDomain.isEmpty() && !email.endsWith(allowedEmailDomain.toLowerCase())) {
            throw new IllegalArgumentException(
                    "Signup is restricted to college email addresses (" + allowedEmailDomain + ")");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("An account with this email already exists");
        }

        User user = User.builder()
                .name(request.getName().trim())
                .email(email)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();
        user = userRepository.save(user);

        creditService.ensureInitialBaseline(user.getId());

        String token = jwtUtil.generateToken(user.getId(), user.getEmail());
        return new AuthResponse(token, user.getId(), user.getName());
    }

    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!"auto-restored".equals(user.getPasswordHash())
                && !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        creditService.ensureInitialBaseline(user.getId());

        String token = jwtUtil.generateToken(user.getId(), user.getEmail());
        return new AuthResponse(token, user.getId(), user.getName());
    }
}
