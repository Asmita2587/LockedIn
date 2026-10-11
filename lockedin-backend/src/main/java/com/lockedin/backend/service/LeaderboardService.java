package com.lockedin.backend.service;

import com.lockedin.backend.dto.LeaderboardEntryResponse;
import com.lockedin.backend.dto.TeachingPortfolioResponse;
import com.lockedin.backend.entity.*;
import com.lockedin.backend.repository.*;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Dual leaderboards (teachers vs learners) and the public teaching portfolio / trust score. */
@Service
public class LeaderboardService {

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final MemberTopicProgressRepository progressRepository;

    public LeaderboardService(UserRepository userRepository, SessionRepository sessionRepository,
                               MemberTopicProgressRepository progressRepository) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.progressRepository = progressRepository;
    }

    public List<LeaderboardEntryResponse> topTeachers(int limit) {
        List<Session> completed = sessionRepository.findAll().stream()
                .filter(s -> s.getStatus() == SessionStatus.COMPLETED && s.getTutor() != null)
                .toList();

        Map<User, Long> byTutor = completed.stream()
                .collect(Collectors.groupingBy(Session::getTutor, Collectors.counting()));

        return byTutor.entrySet().stream()
                .sorted(Map.Entry.<User, Long>comparingByValue().reversed())
                .limit(limit)
                .map(e -> new LeaderboardEntryResponse(e.getKey().getId(), e.getKey().getName(), e.getValue()))
                .toList();
    }

    public List<LeaderboardEntryResponse> topLearners(int limit) {
        List<MemberTopicProgress> done = progressRepository.findAll().stream()
                .filter(p -> p.getStatus() == ProgressStatus.DONE)
                .toList();

        Map<User, Long> byUser = done.stream()
                .collect(Collectors.groupingBy(MemberTopicProgress::getUser, Collectors.counting()));

        return byUser.entrySet().stream()
                .sorted(Map.Entry.<User, Long>comparingByValue().reversed())
                .limit(limit)
                .map(e -> new LeaderboardEntryResponse(e.getKey().getId(), e.getKey().getName(), e.getValue()))
                .toList();
    }

    public TeachingPortfolioResponse getPortfolio(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();

        List<Session> taught = sessionRepository.findByTutorId(userId).stream()
                .filter(s -> s.getStatus() == SessionStatus.COMPLETED)
                .toList();

        List<String> subjects = taught.stream()
                .map(s -> s.getGroup().getSubject().getName())
                .distinct()
                .collect(Collectors.toList());

        return new TeachingPortfolioResponse(userId, user.getName(), user.getTrustScore(), taught.size(), subjects);
    }
}
