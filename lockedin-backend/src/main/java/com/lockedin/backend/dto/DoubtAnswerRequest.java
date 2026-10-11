package com.lockedin.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DoubtAnswerRequest {
    @NotBlank
    private String content;
}
