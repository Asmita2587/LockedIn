package com.lockedin.backend.repository;

import com.lockedin.backend.entity.SessionDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SessionDocumentRepository extends JpaRepository<SessionDocument, Long> {
    List<SessionDocument> findBySessionId(Long sessionId);
}
