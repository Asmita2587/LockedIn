package com.lockedin.backend.repository;

import com.lockedin.backend.entity.SyllabusTopic;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SyllabusTopicRepository extends JpaRepository<SyllabusTopic, Long> {
    List<SyllabusTopic> findByGroupIdOrderByOrderIndexAsc(Long groupId);
}
