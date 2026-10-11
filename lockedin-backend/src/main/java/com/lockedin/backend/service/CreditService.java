package com.lockedin.backend.service;

import com.lockedin.backend.dto.CreditBalanceResponse;
import com.lockedin.backend.entity.*;
import com.lockedin.backend.repository.CreditTransactionRepository;
import com.lockedin.backend.repository.NudgeRepository;
import com.lockedin.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CreditService {

    private final CreditTransactionRepository creditRepo;
    private final UserRepository userRepository;
    private final NudgeRepository nudgeRepository;

    private final int weeklyBaseline;
    private final int teachSessionReward;
    private final int coStudyReward;
    private final int doubtAnswerReward;
    private final int lowBalanceThreshold;

    public CreditService(
            CreditTransactionRepository creditRepo,
            UserRepository userRepository,
            NudgeRepository nudgeRepository,
            @Value("${lockedin.credits.weekly-baseline}") int weeklyBaseline,
            @Value("${lockedin.credits.teach-session-reward}") int teachSessionReward,
            @Value("${lockedin.credits.co-study-reward}") int coStudyReward,
            @Value("${lockedin.credits.doubt-answer-reward}") int doubtAnswerReward,
            @Value("${lockedin.credits.low-balance-threshold}") int lowBalanceThreshold
    ) {
        this.creditRepo = creditRepo;
        this.userRepository = userRepository;
        this.nudgeRepository = nudgeRepository;
        this.weeklyBaseline = weeklyBaseline;
        this.teachSessionReward = teachSessionReward;
        this.coStudyReward = coStudyReward;
        this.doubtAnswerReward = doubtAnswerReward;
        this.lowBalanceThreshold = lowBalanceThreshold;
    }

    public void ensureInitialBaseline(Long userId) {
        if (creditRepo.findByUserIdOrderByCreatedAtDesc(userId).isEmpty()) {
            userRepository.findById(userId).ifPresent(user -> {
                CreditTransaction tx = CreditTransaction.builder()
                        .user(user)
                        .amount(weeklyBaseline)
                        .type(CreditTransactionType.WEEKLY_BASELINE)
                        .note("Initial welcome credits")
                        .build();
                creditRepo.save(tx);
            });
        }
    }

    public int getBalance(Long userId) {
        ensureInitialBaseline(userId);
        return creditRepo.getBalanceForUser(userId);
    }

    public CreditBalanceResponse getBalanceResponse(Long userId) {
        int balance = getBalance(userId);
        return new CreditBalanceResponse(userId, balance, balance <= lowBalanceThreshold);
    }

    public void awardForSessionTaught(Long tutorId) {
        award(tutorId, teachSessionReward, CreditTransactionType.TEACH_SESSION, "Taught a session");
    }

    public void awardForCoStudy(Long userId) {
        award(userId, coStudyReward, CreditTransactionType.CO_STUDY_SESSION, "Co-study session");
    }

    public void awardForDoubtAnswered(Long userId) {
        award(userId, doubtAnswerReward, CreditTransactionType.DOUBT_ANSWER, "Answered a doubt");
    }

    private void award(Long userId, int amount, CreditTransactionType type, String note) {
        userRepository.findById(userId).ifPresent(user -> {
            CreditTransaction tx = CreditTransaction.builder()
                    .user(user)
                    .amount(amount)
                    .type(type)
                    .note(note)
                    .build();
            creditRepo.save(tx);
            checkAndCreateNudgeIfLow(user);
        });
    }

    private void checkAndCreateNudgeIfLow(User user) {
        int balance = creditRepo.getBalanceForUser(user.getId());
        if (balance <= lowBalanceThreshold) {
            Nudge nudge = Nudge.builder()
                    .user(user)
                    .message("Answer 1 doubt to unlock your next session")
                    .build();
            nudgeRepository.save(nudge);
        }
    }

    @Scheduled(cron = "0 0 0 * * MON")
    public void weeklyBaselineTopUp() {
        List<User> allUsers = userRepository.findAll();
        for (User user : allUsers) {
            CreditTransaction tx = CreditTransaction.builder()
                    .user(user)
                    .amount(weeklyBaseline)
                    .type(CreditTransactionType.WEEKLY_BASELINE)
                    .note("Weekly baseline top-up")
                    .build();
            creditRepo.save(tx);
        }
    }
}
