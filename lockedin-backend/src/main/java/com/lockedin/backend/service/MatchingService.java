package com.lockedin.backend.service;

import com.lockedin.backend.dto.MatchSuggestionResponse;
import com.lockedin.backend.entity.*;
import com.lockedin.backend.repository.MatchRepository;
import com.lockedin.backend.repository.SkillRepository;
import com.lockedin.backend.repository.UserRepository;
import com.lockedin.backend.repository.UserSkillRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MatchingService {

    private final UserSkillRepository userSkillRepository;
    private final MatchRepository matchRepository;
    private final UserRepository userRepository;
    private final SkillRepository skillRepository;

    public MatchingService(UserSkillRepository userSkillRepository,
                           MatchRepository matchRepository,
                           UserRepository userRepository,
                           SkillRepository skillRepository) {
        this.userSkillRepository = userSkillRepository;
        this.matchRepository = matchRepository;
        this.userRepository = userRepository;
        this.skillRepository = skillRepository;
    }

    public List<MatchSuggestionResponse> findMatchesFor(Long userId) {
        List<UserSkill> mySkills = userSkillRepository.findByUserId(userId);

        List<UserSkill> wantsToLearn = mySkills.stream()
                .filter(us -> us.getType() == SkillType.LEARN)
                .toList();

        List<UserSkill> canTeach = mySkills.stream()
                .filter(us -> us.getType() == SkillType.TEACH)
                .toList();

        List<MatchSuggestionResponse> suggestions = new ArrayList<>();

        for (UserSkill learnSkill : wantsToLearn) {
            List<UserSkill> teachers = userSkillRepository
                    .findBySkillIdAndType(learnSkill.getSkill().getId(), SkillType.TEACH);

            for (UserSkill teacher : teachers) {
                Long candidateId = teacher.getUser().getId();
                if (candidateId.equals(userId)) continue;

                boolean isMutual = canTeach.stream().anyMatch(mySkill ->
                        userSkillRepository.findBySkillIdAndType(mySkill.getSkill().getId(), SkillType.LEARN)
                                .stream()
                                .anyMatch(theirLearn -> theirLearn.getUser().getId().equals(candidateId))
                );

                suggestions.add(new MatchSuggestionResponse(
                        candidateId,
                        teacher.getUser().getName(),
                        learnSkill.getSkill().getId(),
                        learnSkill.getSkill().getName(),
                        isMutual ? MatchType.MUTUAL : MatchType.ONE_DIRECTIONAL
                ));
            }
        }

        suggestions.sort((a, b) -> {
            if (a.getMatchType() == b.getMatchType()) return 0;
            return a.getMatchType() == MatchType.MUTUAL ? -1 : 1;
        });

        return suggestions;
    }

    public Match acceptMatch(Long requesterId, Long candidateUserId, Long skillId, MatchType matchType) {
        User userA = userRepository.findById(candidateUserId)
                .orElseThrow(() -> new IllegalArgumentException("Candidate user not found"));
        User userB = userRepository.findById(requesterId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new IllegalArgumentException("Skill not found"));

        Match match = Match.builder()
                .userA(userA)
                .userB(userB)
                .skill(skill)
                .matchType(matchType)
                .status(MatchStatus.ACCEPTED)
                .build();

        return matchRepository.save(match);
    }
}
