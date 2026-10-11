package com.lockedin.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email; // must match college domain at signup time

    @JsonIgnore
    @Column(nullable = false)
    private String passwordHash;

    @Builder.Default
    private double trustScore = 0.0;

    @Builder.Default
    private Instant createdAt = Instant.now();
}
