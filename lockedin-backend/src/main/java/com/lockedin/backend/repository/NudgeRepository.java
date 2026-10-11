package com.lockedin.backend.repository;

import com.lockedin.backend.entity.Nudge;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NudgeRepository extends JpaRepository<Nudge, Long> {
    List<Nudge> findByUserIdAndDismissedFalse(Long userId);
}
