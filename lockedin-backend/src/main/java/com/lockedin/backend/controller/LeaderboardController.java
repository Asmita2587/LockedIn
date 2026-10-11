package com.lockedin.backend.controller;

import com.lockedin.backend.dto.LeaderboardEntryResponse;
import com.lockedin.backend.dto.TeachingPortfolioResponse;
import com.lockedin.backend.service.LeaderboardService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    public LeaderboardController(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    @GetMapping("/leaderboard/teachers")
    public List<LeaderboardEntryResponse> topTeachers(@RequestParam(defaultValue = "10") int limit) {
        return leaderboardService.topTeachers(limit);
    }

    @GetMapping("/leaderboard/learners")
    public List<LeaderboardEntryResponse> topLearners(@RequestParam(defaultValue = "10") int limit) {
        return leaderboardService.topLearners(limit);
    }

    /** Public teaching portfolio - linkable on a resume/LinkedIn. */
    @GetMapping("/users/{userId}/portfolio")
    public TeachingPortfolioResponse portfolio(@PathVariable Long userId) {
        return leaderboardService.getPortfolio(userId);
    }
}
