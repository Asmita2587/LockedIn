package com.lockedin.backend.repository;

import com.lockedin.backend.entity.Doubt;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DoubtRepository extends JpaRepository<Doubt, Long> {
    List<Doubt> findByGroupIdOrderByCreatedAtDesc(Long groupId);
    List<Doubt> findByGroupIdAndResolvedFalse(Long groupId);
}
