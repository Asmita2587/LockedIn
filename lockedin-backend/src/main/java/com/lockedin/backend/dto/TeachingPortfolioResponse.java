package com.lockedin.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class TeachingPortfolioResponse {
    private Long userId;
    private String name;
    private double trustScore;
    private long sessionsTaught;
    private List<String> subjectsCovered;
}
