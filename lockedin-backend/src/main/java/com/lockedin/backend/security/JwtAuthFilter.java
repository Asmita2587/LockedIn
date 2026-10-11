package com.lockedin.backend.security;

import com.lockedin.backend.entity.User;
import com.lockedin.backend.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public JwtAuthFilter(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            if (jwtUtil.isValid(token)) {
                Long tokenUserId = jwtUtil.extractUserId(token);
                String email = jwtUtil.extractEmail(token);

                Long resolvedUserId = null;
                Optional<User> byId = userRepository.findById(tokenUserId);
                if (byId.isPresent()) {
                    resolvedUserId = byId.get().getId();
                } else if (email != null && !email.isBlank()) {
                    Optional<User> byEmail = userRepository.findByEmail(email.toLowerCase());
                    if (byEmail.isPresent()) {
                        resolvedUserId = byEmail.get().getId();
                    } else {
                        String fallbackName = email.contains("@") ? email.substring(0, email.indexOf('@')) : "Student";
                        User created = userRepository.save(User.builder()
                                .name(fallbackName)
                                .email(email.toLowerCase())
                                .passwordHash("auto-restored")
                                .build());
                        resolvedUserId = created.getId();
                    }
                }

                if (resolvedUserId != null) {
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(resolvedUserId, null, Collections.emptyList());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
