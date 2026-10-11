package com.lockedin.backend.service;

import com.lockedin.backend.entity.*;
import com.lockedin.backend.repository.DoubtAnswerRepository;
import com.lockedin.backend.repository.DoubtRepository;
import com.lockedin.backend.repository.StudyGroupRepository;
import com.lockedin.backend.repository.UserRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DoubtService {

    private final DoubtRepository doubtRepository;
    private final DoubtAnswerRepository doubtAnswerRepository;
    private final StudyGroupRepository groupRepository;
    private final UserRepository userRepository;
    private final CreditService creditService;
    private final SimpMessagingTemplate messagingTemplate;

    public DoubtService(DoubtRepository doubtRepository,
                        DoubtAnswerRepository doubtAnswerRepository,
                        StudyGroupRepository groupRepository,
                        UserRepository userRepository,
                        CreditService creditService,
                        SimpMessagingTemplate messagingTemplate) {
        this.doubtRepository = doubtRepository;
        this.doubtAnswerRepository = doubtAnswerRepository;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.creditService = creditService;
        this.messagingTemplate = messagingTemplate;
    }

    public Map<String, Object> postDoubt(Long groupId, Long userId, String title, String description) {
        StudyGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Doubt doubt = Doubt.builder()
                .group(group)
                .postedBy(user)
                .title(title.trim())
                .description(description == null || description.isBlank() ? null : description.trim())
                .build();
        doubt = doubtRepository.save(doubt);

        Map<String, Object> dto = toDoubtMap(doubt);
        try {
            messagingTemplate.convertAndSend("/topic/groups/" + groupId + "/doubts", dto);
        } catch (Exception ignored) {
        }
        return dto;
    }

    public List<Doubt> getOpenDoubts(Long groupId) {
        return doubtRepository.findByGroupIdAndResolvedFalse(groupId);
    }

    public DoubtAnswer answerDoubt(Long doubtId, Long answeredByUserId, String content) {
        Doubt doubt = doubtRepository.findById(doubtId)
                .orElseThrow(() -> new IllegalArgumentException("Doubt not found"));
        User user = userRepository.findById(answeredByUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        DoubtAnswer answer = DoubtAnswer.builder()
                .doubt(doubt)
                .answeredBy(user)
                .content(content.trim())
                .build();
        answer = doubtAnswerRepository.save(answer);

        creditService.awardForDoubtAnswered(answeredByUserId);
        return answer;
    }

    public Map<String, Object> resolveDoubt(Long doubtId) {
        Doubt doubt = doubtRepository.findById(doubtId)
                .orElseThrow(() -> new IllegalArgumentException("Doubt not found"));
        doubt.setResolved(true);
        doubt = doubtRepository.save(doubt);
        return toDoubtMap(doubt);
    }

    public List<Map<String, Object>> getDoubtsWithAnswers(Long groupId) {
        return doubtRepository.findByGroupIdOrderByCreatedAtDesc(groupId).stream()
                .map(this::toDoubtMap)
                .toList();
    }

    private Map<String, Object> toDoubtMap(Doubt d) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", d.getId());
        m.put("title", d.getTitle());
        m.put("description", d.getDescription());
        m.put("resolved", d.isResolved());
        m.put("createdAt", d.getCreatedAt());
        m.put("postedBy", d.getPostedBy() == null ? "Student" : d.getPostedBy().getName());
        m.put("answers", doubtAnswerRepository.findByDoubtId(d.getId()).stream().map(a -> {
            Map<String, Object> am = new LinkedHashMap<>();
            am.put("id", a.getId());
            am.put("content", a.getContent());
            am.put("createdAt", a.getCreatedAt());
            am.put("answeredBy", a.getAnsweredBy() == null ? "Student" : a.getAnsweredBy().getName());
            return am;
        }).toList());
        return m;
    }
}
