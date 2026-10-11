package com.lockedin.backend.controller;

import com.lockedin.backend.entity.Session;
import com.lockedin.backend.entity.SessionDocument;
import com.lockedin.backend.entity.SessionType;
import com.lockedin.backend.service.SessionService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/groups/{groupId}/sessions")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping
    public Session schedule(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long groupId,
            @RequestParam(required = false) Long tutorId,
            @RequestParam(defaultValue = "CO_STUDY") SessionType type,
            @RequestParam(defaultValue = "false") boolean isAnonymous,
            @RequestParam(defaultValue = "true") boolean videoEnabled,
            @RequestParam(required = false) Instant scheduledAt
    ) {
        Long resolvedTutorId = tutorId != null ? tutorId : (type == SessionType.TUTOR ? userId : null);
        Instant when = scheduledAt != null ? scheduledAt : Instant.now();
        return sessionService.schedule(groupId, resolvedTutorId, type, isAnonymous, videoEnabled, when);
    }

    @PostMapping("/{sessionId}/start")
    public Session start(@PathVariable Long sessionId) {
        return sessionService.start(sessionId);
    }

    @PostMapping("/{sessionId}/complete")
    public Session complete(
            @PathVariable Long sessionId,
            @RequestParam(required = false) List<Long> coStudyParticipantIds
    ) {
        return sessionService.complete(sessionId, coStudyParticipantIds == null ? List.of() : coStudyParticipantIds);
    }

    @PostMapping("/{sessionId}/documents")
    public SessionDocument attachDocument(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long sessionId,
            @RequestParam String fileUrl,
            @RequestParam String fileName
    ) {
        return sessionService.attachDocument(sessionId, userId, fileUrl, fileName);
    }

    @GetMapping
    public List<Session> groupSessions(@PathVariable Long groupId) {
        return sessionService.getGroupSessions(groupId);
    }
}
