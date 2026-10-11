package com.lockedin.backend.repository;

import com.lockedin.backend.entity.MemberTopicProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MemberTopicProgressRepository extends JpaRepository<MemberTopicProgress, Long> {
    List<MemberTopicProgress> findByUserId(Long userId);
    List<MemberTopicProgress> findByTopicGroupId(Long groupId);
    Optional<MemberTopicProgress> findByUserIdAndTopicId(Long userId, Long topicId);
}
