package com.lockedin.backend.service;

import com.lockedin.backend.dto.UserSkillRequest;
import com.lockedin.backend.entity.Skill;
import com.lockedin.backend.entity.User;
import com.lockedin.backend.entity.UserSkill;
import com.lockedin.backend.repository.SkillRepository;
import com.lockedin.backend.repository.UserRepository;
import com.lockedin.backend.repository.UserSkillRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SkillService {

    private final SkillRepository skillRepository;
    private final UserSkillRepository userSkillRepository;
    private final UserRepository userRepository;

    public SkillService(SkillRepository skillRepository,
                        UserSkillRepository userSkillRepository,
                        UserRepository userRepository) {
        this.skillRepository = skillRepository;
        this.userSkillRepository = userSkillRepository;
        this.userRepository = userRepository;
    }

    public UserSkill addUserSkill(Long userId, UserSkillRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found. Please log in again."));

        String cleanName = request.getSkillName().trim();
        Skill skill = skillRepository.findByNameIgnoreCase(cleanName)
                .orElseGet(() -> skillRepository.save(Skill.builder().name(cleanName).build()));

        Optional<UserSkill> existing = userSkillRepository.findByUserId(userId).stream()
                .filter(us -> us.getSkill().getId().equals(skill.getId()) && us.getType() == request.getType())
                .findFirst();

        if (existing.isPresent()) {
            UserSkill us = existing.get();
            us.setProficiency(request.getProficiency());
            return userSkillRepository.save(us);
        }

        UserSkill userSkill = UserSkill.builder()
                .user(user)
                .skill(skill)
                .type(request.getType())
                .proficiency(request.getProficiency())
                .build();

        return userSkillRepository.save(userSkill);
    }

    public List<UserSkill> getUserSkills(Long userId) {
        return userSkillRepository.findByUserId(userId);
    }

    public void removeUserSkill(Long userId, Long userSkillId) {
        userSkillRepository.findById(userSkillId).ifPresent(us -> {
            if (us.getUser() != null && us.getUser().getId().equals(userId)) {
                userSkillRepository.delete(us);
            }
        });
    }
}
