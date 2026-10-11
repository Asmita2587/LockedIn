package com.lockedin.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LeaderboardEntryResponse {
    private Long userId;
    private String name;
    private double score; // sessions taught / ratings for teachers, topics completed / streak for learners
}
