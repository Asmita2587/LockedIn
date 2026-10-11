package com.lockedin.backend.repository;

import com.lockedin.backend.entity.DoubtAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DoubtAnswerRepository extends JpaRepository<DoubtAnswer, Long> {
    List<DoubtAnswer> findByDoubtId(Long doubtId);
}
