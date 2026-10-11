package com.lockedin.backend.service;

import com.lockedin.backend.entity.*;
import com.lockedin.backend.repository.GroupWikiEntryRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/** Auto-compiles session notes into a running, per-topic group wiki. */
@Service
public class WikiService {

    private final GroupWikiEntryRepository wikiRepository;

    public WikiService(GroupWikiEntryRepository wikiRepository) {
        this.wikiRepository = wikiRepository;
    }

    public GroupWikiEntry appendSessionNotes(Long groupId, Long topicId, Long sessionId, String notes) {
        GroupWikiEntry entry = GroupWikiEntry.builder()
                .group(StudyGroup.builder().id(groupId).build())
                .topic(SyllabusTopic.builder().id(topicId).build())
                .sourceSession(Session.builder().id(sessionId).build())
                .content(notes)
                .updatedAt(Instant.now())
                .build();
        return wikiRepository.save(entry);
    }

    public List<GroupWikiEntry> getWikiForGroup(Long groupId) {
        return wikiRepository.findByGroupIdOrderByUpdatedAtDesc(groupId);
    }
}
