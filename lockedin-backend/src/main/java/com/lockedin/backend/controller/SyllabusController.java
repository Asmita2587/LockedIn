package com.lockedin.backend.controller;

import com.lockedin.backend.entity.MemberTopicProgress;
import com.lockedin.backend.entity.ProgressStatus;
import com.lockedin.backend.entity.SyllabusTopic;
import com.lockedin.backend.service.SyllabusService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/groups/{groupId}/syllabus")
public class SyllabusController {

    private final SyllabusService syllabusService;

    public SyllabusController(SyllabusService syllabusService) {
        this.syllabusService = syllabusService;
    }

    @PostMapping("/topics")
    public SyllabusTopic addTopic(
            @PathVariable Long groupId,
            @RequestParam String title,
            @RequestParam(defaultValue = "0") int orderIndex
    ) {
        return syllabusService.addTopic(groupId, title, orderIndex);
    }

    @GetMapping("/topics")
    public List<SyllabusTopic> topics(@PathVariable Long groupId) {
        return syllabusService.getTopics(groupId);
    }

    @GetMapping("/progress")
    public Map<Long, String> myProgress(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long groupId
    ) {
        return syllabusService.getUserProgressForGroup(userId, groupId);
    }

    @PutMapping("/topics/{topicId}/progress")
    public MemberTopicProgress updateProgress(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long topicId,
            @RequestParam ProgressStatus status
    ) {
        return syllabusService.updateProgress(userId, topicId, status);
    }

    @GetMapping("/lag-summary")
    public Map<String, Long> lagSummary(@PathVariable Long groupId) {
        return syllabusService.getGroupLagSummary(groupId);
    }
}
