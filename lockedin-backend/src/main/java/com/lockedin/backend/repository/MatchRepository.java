package com.lockedin.backend.repository;

import com.lockedin.backend.entity.Match;
import com.lockedin.backend.entity.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findByUserAIdOrUserBId(Long userAId, Long userBId);
    List<Match> findByUserBIdAndStatus(Long userBId, MatchStatus status);
}
