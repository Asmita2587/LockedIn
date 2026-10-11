package com.lockedin.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "group_wiki_entries")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GroupWikiEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "group_id")
    private StudyGroup group;

    @ManyToOne(optional = false)
    @JoinColumn(name = "topic_id")
    private SyllabusTopic topic;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne
    @JoinColumn(name = "source_session_id")
    private Session sourceSession;

    @Builder.Default
    private Instant updatedAt = Instant.now();
}
