package com.lockedin.backend.service;

import com.lockedin.backend.entity.*;
import com.lockedin.backend.repository.MemberTopicProgressRepository;
import com.lockedin.backend.repository.StudyGroupRepository;
import com.lockedin.backend.repository.SyllabusTopicRepository;
import com.lockedin.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SyllabusService {

    private final SyllabusTopicRepository topicRepository;
    private final MemberTopicProgressRepository progressRepository;
    private final StudyGroupRepository groupRepository;
    private final UserRepository userRepository;

    public SyllabusService(SyllabusTopicRepository topicRepository,
                           MemberTopicProgressRepository progressRepository,
                           StudyGroupRepository groupRepository,
                           UserRepository userRepository) {
        this.topicRepository = topicRepository;
        this.progressRepository = progressRepository;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
    }

    public SyllabusTopic addTopic(Long groupId, String title, int orderIndex) {
        StudyGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
        SyllabusTopic topic = SyllabusTopic.builder()
                .group(group)
                .title(title.trim())
                .orderIndex(orderIndex)
                .build();
        return topicRepository.save(topic);
    }

    public List<SyllabusTopic> getTopics(Long groupId) {
        return topicRepository.findByGroupIdOrderByOrderIndexAsc(groupId);
    }

    public MemberTopicProgress updateProgress(Long userId, Long topicId, ProgressStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        SyllabusTopic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new IllegalArgumentException("Topic not found"));

        MemberTopicProgress progress = progressRepository.findByUserIdAndTopicId(userId, topicId)
                .orElse(MemberTopicProgress.builder()
                        .user(user)
                        .topic(topic)
                        .build());

        progress.setStatus(status);
        progress.setUpdatedAt(Instant.now());
        return progressRepository.save(progress);
    }

    public Map<Long, String> getUserProgressForGroup(Long userId, Long groupId) {
        Map<Long, String> result = new LinkedHashMap<>();
        progressRepository.findByTopicGroupId(groupId).stream()
                .filter(p -> p.getUser() != null && p.getUser().getId().equals(userId))
                .forEach(p -> result.put(p.getTopic().getId(), p.getStatus().name()));
        return result;
    }

    public Map<String, Long> getGroupLagSummary(Long groupId) {
        List<MemberTopicProgress> allProgress = progressRepository.findByTopicGroupId(groupId);
        return allProgress.stream()
                .filter(p -> p.getStatus() != ProgressStatus.DONE)
                .collect(Collectors.groupingBy(p -> p.getTopic().getTitle(), Collectors.counting()));
    }
}
