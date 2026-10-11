package com.lockedin.backend.controller;

import com.lockedin.backend.dto.DoubtAnswerRequest;
import com.lockedin.backend.entity.Doubt;
import com.lockedin.backend.entity.DoubtAnswer;
import com.lockedin.backend.service.DoubtService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/groups/{groupId}/doubts")
public class DoubtController {

    private final DoubtService doubtService;

    public DoubtController(DoubtService doubtService) {
        this.doubtService = doubtService;
    }

    @PostMapping
    public Map<String, Object> post(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long groupId,
            @RequestParam String title,
            @RequestParam(required = false) String description
    ) {
        return doubtService.postDoubt(groupId, userId, title, description);
    }

    @GetMapping
    public List<Map<String, Object>> allDoubts(@PathVariable Long groupId) {
        return doubtService.getDoubtsWithAnswers(groupId);
    }

    @GetMapping("/open")
    public List<Doubt> openDoubts(@PathVariable Long groupId) {
        return doubtService.getOpenDoubts(groupId);
    }

    @PostMapping("/{doubtId}/answers")
    public DoubtAnswer answer(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long doubtId,
            @Valid @RequestBody DoubtAnswerRequest request
    ) {
        return doubtService.answerDoubt(doubtId, userId, request.getContent());
    }

    @PostMapping("/{doubtId}/resolve")
    public Map<String, Object> resolve(@PathVariable Long doubtId) {
        return doubtService.resolveDoubt(doubtId);
    }
}
