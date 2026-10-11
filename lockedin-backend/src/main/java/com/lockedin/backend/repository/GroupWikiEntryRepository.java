package com.lockedin.backend.repository;

import com.lockedin.backend.entity.GroupWikiEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GroupWikiEntryRepository extends JpaRepository<GroupWikiEntry, Long> {
    List<GroupWikiEntry> findByGroupIdOrderByUpdatedAtDesc(Long groupId);
    List<GroupWikiEntry> findByTopicId(Long topicId);
}
