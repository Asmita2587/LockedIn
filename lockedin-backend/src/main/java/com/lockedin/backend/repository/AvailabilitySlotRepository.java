package com.lockedin.backend.repository;

import com.lockedin.backend.entity.AvailabilitySlot;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AvailabilitySlotRepository extends JpaRepository<AvailabilitySlot, Long> {
    List<AvailabilitySlot> findByUserId(Long userId);
}
