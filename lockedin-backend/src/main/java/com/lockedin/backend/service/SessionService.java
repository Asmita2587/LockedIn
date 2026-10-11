package com.lockedin.backend.service;

import com.lockedin.backend.entity.*;
import com.lockedin.backend.repository.SessionDocumentRepository;
import com.lockedin.backend.repository.SessionRepository;
import com.lockedin.backend.repository.StudyGroupRepository;
import com.lockedin.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class SessionService {

    private final SessionRepository sessionRepository;
    private final SessionDocumentRepository sessionDocumentRepository;
    private final StudyGroupRepository groupRepository;
    private final UserRepository userRepository;
    private final CreditService creditService;

    public SessionService(SessionRepository sessionRepository,
                          SessionDocumentRepository sessionDocumentRepository,
                          StudyGroupRepository groupRepository,
                          UserRepository userRepository,
                          CreditService creditService) {
        this.sessionRepository = sessionRepository;
        this.sessionDocumentRepository = sessionDocumentRepository;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.creditService = creditService;
    }

    public Session schedule(Long groupId, Long tutorId, SessionType type, boolean isAnonymous,
                            boolean videoEnabled, Instant scheduledAt) {
        StudyGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
        User tutor = tutorId == null ? null : userRepository.findById(tutorId).orElse(null);

        Session session = Session.builder()
                .group(group)
                .tutor(tutor)
                .type(type)
                .isAnonymous(isAnonymous)
                .videoEnabled(videoEnabled)
                .scheduledAt(scheduledAt == null ? Instant.now() : scheduledAt)
                .status(SessionStatus.SCHEDULED)
                .build();
        return sessionRepository.save(session);
    }

    public Session start(Long sessionId) {
        Session session = sessionRepository.findById(sessionId).orElseThrow();
        session.setStatus(SessionStatus.IN_PROGRESS);
        return sessionRepository.save(session);
    }

    public Session complete(Long sessionId, List<Long> coStudyParticipantIds) {
        Session session = sessionRepository.findById(sessionId).orElseThrow();
        session.setStatus(SessionStatus.COMPLETED);
        session = sessionRepository.save(session);

        if (session.getType() == SessionType.TUTOR && session.getTutor() != null) {
            creditService.awardForSessionTaught(session.getTutor().getId());
        } else if (session.getType() == SessionType.CO_STUDY) {
            for (Long participantId : coStudyParticipantIds) {
                creditService.awardForCoStudy(participantId);
            }
        }

        return session;
    }

    public SessionDocument attachDocument(Long sessionId, Long uploadedByUserId, String fileUrl, String fileName) {
        Session session = sessionRepository.findById(sessionId).orElseThrow();
        User uploader = userRepository.findById(uploadedByUserId).orElseThrow();
        SessionDocument doc = SessionDocument.builder()
                .session(session)
                .uploadedBy(uploader)
                .fileUrl(fileUrl)
                .fileName(fileName)
                .build();
        return sessionDocumentRepository.save(doc);
    }

    public List<Session> getGroupSessions(Long groupId) {
        return sessionRepository.findByGroupId(groupId);
    }
}
