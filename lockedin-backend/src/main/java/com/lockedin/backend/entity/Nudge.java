package com.lockedin.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "nudges")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Nudge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String message;

    @Builder.Default
    private boolean dismissed = false;

    @Builder.Default
    private Instant createdAt = Instant.now();
}
