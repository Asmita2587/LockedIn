package com.lockedin.backend.controller;

import com.lockedin.backend.dto.RatingRequest;
import com.lockedin.backend.entity.Rating;
import com.lockedin.backend.service.RatingService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sessions/{sessionId}/ratings")
public class RatingController {

    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @PostMapping
    public Rating submit(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long sessionId,
            @Valid @RequestBody RatingRequest request
    ) {
        return ratingService.submitRating(sessionId, userId, request.getRatedUserId(),
                request.getScore(), request.getComment());
    }
}
