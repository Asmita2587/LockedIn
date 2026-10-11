package com.lockedin.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "syllabus_topics")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SyllabusTopic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "group_id")
    private StudyGroup group;

    @Column(nullable = false)
    private String title;

    @Builder.Default
    private int orderIndex = 0;
}
