package com.lockedin.backend.controller;

import com.lockedin.backend.dto.MatchSuggestionResponse;
import com.lockedin.backend.entity.Match;
import com.lockedin.backend.entity.MatchType;
import com.lockedin.backend.service.MatchingService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/matches")
public class MatchController {

    private final MatchingService matchingService;

    public MatchController(MatchingService matchingService) {
        this.matchingService = matchingService;
    }

    @GetMapping("/suggestions")
    public List<MatchSuggestionResponse> getSuggestions(@AuthenticationPrincipal Long userId) {
        return matchingService.findMatchesFor(userId);
    }

    @PostMapping("/accept")
    public Match accept(
            @AuthenticationPrincipal Long userId,
            @RequestParam Long candidateUserId,
            @RequestParam Long skillId,
            @RequestParam MatchType matchType
    ) {
        return matchingService.acceptMatch(userId, candidateUserId, skillId, matchType);
    }
}
