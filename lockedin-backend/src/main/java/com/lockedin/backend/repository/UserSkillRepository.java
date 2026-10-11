package com.lockedin.backend.repository;

import com.lockedin.backend.entity.SkillType;
import com.lockedin.backend.entity.UserSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserSkillRepository extends JpaRepository<UserSkill, Long> {
    List<UserSkill> findByUserId(Long userId);
    List<UserSkill> findBySkillIdAndType(Long skillId, SkillType type);
}
