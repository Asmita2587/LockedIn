package com.lockedin.backend.controller;

import com.lockedin.backend.dto.UserSkillRequest;
import com.lockedin.backend.entity.UserSkill;
import com.lockedin.backend.service.SkillService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/skills")
public class SkillController {

    private final SkillService skillService;

    public SkillController(SkillService skillService) {
        this.skillService = skillService;
    }

    @PostMapping("/me")
    public UserSkill addSkill(@AuthenticationPrincipal Long userId, @Valid @RequestBody UserSkillRequest request) {
        return skillService.addUserSkill(userId, request);
    }

    @GetMapping("/me")
    public List<UserSkill> mySkills(@AuthenticationPrincipal Long userId) {
        return skillService.getUserSkills(userId);
    }

    @DeleteMapping("/me/{userSkillId}")
    public void removeSkill(@AuthenticationPrincipal Long userId, @PathVariable Long userSkillId) {
        skillService.removeUserSkill(userId, userSkillId);
    }
}
