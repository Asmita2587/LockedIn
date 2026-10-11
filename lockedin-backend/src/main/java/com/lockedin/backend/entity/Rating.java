package com.lockedin.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "ratings")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "session_id")
    private Session session;

    @ManyToOne(optional = false)
    @JoinColumn(name = "rated_by")
    private User ratedBy;

    @ManyToOne(optional = false)
    @JoinColumn(name = "rated_user")
    private User ratedUser;

    @Column(nullable = false)
    private int score;

    private String comment;

    @Builder.Default
    private Instant createdAt = Instant.now();
}
