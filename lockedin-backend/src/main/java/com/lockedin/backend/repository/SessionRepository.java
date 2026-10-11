package com.lockedin.backend.repository;

import com.lockedin.backend.entity.Session;
import com.lockedin.backend.entity.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;

public interface SessionRepository extends JpaRepository<Session, Long> {
    List<Session> findByGroupId(Long groupId);
    List<Session> findByTutorId(Long tutorId);

    List<Session> findByStatusAndReminderSentFalseAndScheduledAtBetween(
            SessionStatus status, Instant windowStart, Instant windowEnd);
}
