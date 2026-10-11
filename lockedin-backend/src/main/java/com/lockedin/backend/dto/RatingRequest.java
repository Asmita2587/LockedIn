package com.lockedin.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RatingRequest {
    @NotNull
    private Long ratedUserId;

    @Min(1) @Max(5)
    private int score;

    private String comment;
}
