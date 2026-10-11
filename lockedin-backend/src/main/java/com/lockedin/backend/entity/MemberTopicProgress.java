package com.lockedin.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "member_topic_progress", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "topic_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MemberTopicProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "topic_id")
    private SyllabusTopic topic;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ProgressStatus status = ProgressStatus.NOT_STARTED;

    @Builder.Default
    private Instant updatedAt = Instant.now();
}
