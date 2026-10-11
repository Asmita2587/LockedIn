package com.lockedin.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "doubt_answers")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DoubtAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "doubt_id")
    private Doubt doubt;

    @ManyToOne(optional = false)
    @JoinColumn(name = "answered_by")
    private User answeredBy;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Builder.Default
    private Instant createdAt = Instant.now();
}
