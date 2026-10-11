package com.lockedin.backend.dto;

import com.lockedin.backend.entity.ProficiencyLevel;
import com.lockedin.backend.entity.SkillType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserSkillRequest {
    @NotBlank
    private String skillName;

    @NotNull
    private SkillType type;

    @NotNull
    private ProficiencyLevel proficiency;
}
