package com.lockedin.backend.service;

import com.lockedin.backend.dto.MatchSuggestionResponse;
import com.lockedin.backend.entity.*;
import com.lockedin.backend.repository.MatchRepository;
import com.lockedin.backend.repository.SkillRepository;
import com.lockedin.backend.repository.UserRepository;
import com.lockedin.backend.repository.UserSkillRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    private boolean skillsMatch(Skill a, Skill b) {
        if (a == null || b == null) return false;
        if (a.getId() != null && a.getId().equals(b.getId())) return true;
        String n1 = a.getName() != null ? a.getName().trim().toLowerCase() : "";
        String n2 = b.getName() != null ? b.getName().trim().toLowerCase() : "";
        if (n1.isEmpty() || n2.isEmpty()) return false;
        if (n1.equals(n2)) return true;
        return (n1.length() >= 2 && n2.length() >= 2) && (n1.contains(n2) || n2.contains(n1));
    }

    public List<MatchSuggestionResponse> findMatchesFor(Long userId) {
        List<UserSkill> allUserSkills = userSkillRepository.findAll();
        List<UserSkill> mySkills = userSkillRepository.findByUserId(userId);

        List<UserSkill> wantsToLearn = mySkills.stream()
                .filter(us -> us.getType() == SkillType.LEARN)
                .toList();

        List<UserSkill> canTeach = mySkills.stream()
                .filter(us -> us.getType() == SkillType.TEACH)
                .toList();

        List<MatchSuggestionResponse> suggestions = new ArrayList<>();
        Set<String> seenKeys = new HashSet<>();

        // 1. Primary: For every skill I want to LEARN, find students who TEACH it
        for (UserSkill learnSkill : wantsToLearn) {
            for (UserSkill teacher : allUserSkills) {
                if (teacher.getType() != SkillType.TEACH || teacher.getUser() == null) continue;
                Long candidateId = teacher.getUser().getId();
                if (candidateId == null || candidateId.equals(userId)) continue;
                if (!skillsMatch(learnSkill.getSkill(), teacher.getSkill())) continue;

                String key = "LEARN:" + candidateId + ":" + teacher.getSkill().getId();
                if (!seenKeys.add(key)) continue;

                boolean isMutual = canTeach.stream().anyMatch(myTeach ->
                        allUserSkills.stream().anyMatch(theirLearn ->
                                theirLearn.getType() == SkillType.LEARN &&
                                theirLearn.getUser() != null &&
                                candidateId.equals(theirLearn.getUser().getId()) &&
                                skillsMatch(myTeach.getSkill(), theirLearn.getSkill())
                        )
                );

                suggestions.add(new MatchSuggestionResponse(
                        candidateId,
                        teacher.getUser().getName(),
                        teacher.getSkill().getId(),
                        teacher.getSkill().getName(),
                        isMutual ? MatchType.MUTUAL : MatchType.ONE_DIRECTIONAL,
                        teacher.getProficiency() != null ? teacher.getProficiency().name() : "INTERMEDIATE",
                        "LEARN"
                ));
            }
        }

        // 2. Secondary: For every skill I TEACH, find students who want to LEARN it
        for (UserSkill teachSkill : canTeach) {
            for (UserSkill learner : allUserSkills) {
                if (learner.getType() != SkillType.LEARN || learner.getUser() == null) continue;
                Long candidateId = learner.getUser().getId();
                if (candidateId == null || candidateId.equals(userId)) continue;
                if (!skillsMatch(teachSkill.getSkill(), learner.getSkill())) continue;

                String key = "TEACH:" + candidateId + ":" + teachSkill.getSkill().getId();
                if (!seenKeys.add(key)) continue;

                boolean isMutual = wantsToLearn.stream().anyMatch(myLearn ->
                        allUserSkills.stream().anyMatch(theirTeach ->
                                theirTeach.getType() == SkillType.TEACH &&
                                theirTeach.getUser() != null &&
                                candidateId.equals(theirTeach.getUser().getId()) &&
                                skillsMatch(myLearn.getSkill(), theirTeach.getSkill())
                        )
                );

                suggestions.add(new MatchSuggestionResponse(
                        candidateId,
                        learner.getUser().getName(),
                        teachSkill.getSkill().getId(),
                        teachSkill.getSkill().getName(),
                        isMutual ? MatchType.MUTUAL : MatchType.ONE_DIRECTIONAL,
                        teachSkill.getProficiency() != null ? teachSkill.getProficiency().name() : "INTERMEDIATE",
                        "TEACH"
                ));
            }
        }

        suggestions.sort((a, b) -> {
            // Put "LEARN" (tutors who teach what I want to learn) first, then MUTUAL first
            if (!a.getDirection().equals(b.getDirection())) {
                return "LEARN".equals(a.getDirection()) ? -1 : 1;
            }
            if (a.getMatchType() == b.getMatchType()) return 0;
            return a.getMatchType() == MatchType.MUTUAL ? -1 : 1;
        });

        return suggestions;
    }

    public List<UserSkill> findAllTutors(Long userId) {
        return userSkillRepository.findAll().stream()
                .filter(us -> us.getType() == SkillType.TEACH && us.getUser() != null && !us.getUser().getId().equals(userId))
                .toList();
    }

    public List<Match> getAcceptedMatches(Long userId) {
        return matchRepository.findByUserAIdOrUserBId(userId, userId);
    }

    public Match acceptMatch(Long requesterId, Long candidateUserId, Long skillId, MatchType matchType) {
        User userA = userRepository.findById(candidateUserId)
                .orElseThrow(() -> new IllegalArgumentException("Candidate user not found"));
        User userB = userRepository.findById(requesterId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new IllegalArgumentException("Skill not found"));

        List<Match> existing = matchRepository.findByUserAIdOrUserBId(requesterId, requesterId);
        for (Match m : existing) {
            boolean sameSkill = m.getSkill() != null && m.getSkill().getId().equals(skillId);
            boolean samePair = (m.getUserA().getId().equals(candidateUserId) && m.getUserB().getId().equals(requesterId)) ||
                               (m.getUserA().getId().equals(requesterId) && m.getUserB().getId().equals(candidateUserId));
            if (sameSkill && samePair) {
                return m;
            }
        }

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
