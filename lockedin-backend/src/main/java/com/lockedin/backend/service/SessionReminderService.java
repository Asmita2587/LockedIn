package com.lockedin.backend.service;

import com.lockedin.backend.entity.GroupMember;
import com.lockedin.backend.entity.Session;
import com.lockedin.backend.entity.SessionStatus;
import com.lockedin.backend.repository.GroupMemberRepository;
import com.lockedin.backend.repository.SessionRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Runs every 15 minutes, looks for SCHEDULED sessions starting in roughly
 * the next hour that haven't been reminded yet, and emails every member
 * of that session's group. This is what actually uses the Spring Mail
 * config in application.properties - EmailService just sends one message,
 * this class decides who and when.
 */
@Service
public class SessionReminderService {

    private final SessionRepository sessionRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final EmailService emailService;

    public SessionReminderService(SessionRepository sessionRepository,
                                   GroupMemberRepository groupMemberRepository,
                                   EmailService emailService) {
        this.sessionRepository = sessionRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.emailService = emailService;
    }

    @Scheduled(fixedRate = 15 * 60 * 1000) // every 15 minutes
    public void sendUpcomingSessionReminders() {
        Instant now = Instant.now();
        Instant windowStart = now.plus(55, ChronoUnit.MINUTES);
        Instant windowEnd = now.plus(65, ChronoUnit.MINUTES);

        List<Session> upcoming = sessionRepository
                .findByStatusAndReminderSentFalseAndScheduledAtBetween(
                        SessionStatus.SCHEDULED, windowStart, windowEnd);

        for (Session session : upcoming) {
            List<GroupMember> members = groupMemberRepository.findByGroupId(session.getGroup().getId());

            String subject = "LockedIn — your session starts in about an hour";
            String body = "Your " + session.getType() + " session is scheduled for "
                    + session.getScheduledAt() + ". See you there!";

            for (GroupMember member : members) {
                emailService.send(member.getUser().getEmail(), subject, body);
            }

            session.setReminderSent(true);
            sessionRepository.save(session);
        }
    }
}
