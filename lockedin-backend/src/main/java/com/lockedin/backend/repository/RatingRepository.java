package com.lockedin.backend.repository;

import com.lockedin.backend.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RatingRepository extends JpaRepository<Rating, Long> {
    List<Rating> findByRatedUserId(Long userId);
    List<Rating> findBySessionId(Long sessionId);
}
