package com.lockedin.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "sessions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "group_id")
    private StudyGroup group;

    @ManyToOne
    @JoinColumn(name = "tutor_id")
    private User tutor; // null for CO_STUDY

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionType type;

    @Builder.Default
    private boolean isAnonymous = false;

    @Builder.Default
    private boolean videoEnabled = true;

    private Instant scheduledAt;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SessionStatus status = SessionStatus.SCHEDULED;

    @Builder.Default
    private boolean reminderSent = false;

    @Builder.Default
    private Instant createdAt = Instant.now();
}
