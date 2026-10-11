package com.lockedin.backend.service;

import com.lockedin.backend.entity.AvailabilitySlot;
import com.lockedin.backend.entity.User;
import com.lockedin.backend.repository.AvailabilitySlotRepository;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AvailabilityService {

    private final AvailabilitySlotRepository slotRepository;

    public AvailabilityService(AvailabilitySlotRepository slotRepository) {
        this.slotRepository = slotRepository;
    }

    public AvailabilitySlot addSlot(Long userId, DayOfWeek day, LocalTime start, LocalTime end) {
        AvailabilitySlot slot = AvailabilitySlot.builder()
                .user(User.builder().id(userId).build())
                .dayOfWeek(day)
                .startTime(start)
                .endTime(end)
                .build();
        return slotRepository.save(slot);
    }

    public List<AvailabilitySlot> getSlots(Long userId) {
        return slotRepository.findByUserId(userId);
    }

    /** Overlapping windows (same day, overlapping time range) - used when scheduling a session. */
    public List<AvailabilitySlot> findOverlap(Long userAId, Long userBId) {
        List<AvailabilitySlot> aSlots = slotRepository.findByUserId(userAId);
        List<AvailabilitySlot> bSlots = slotRepository.findByUserId(userBId);

        List<AvailabilitySlot> overlaps = new ArrayList<>();
        for (AvailabilitySlot a : aSlots) {
            for (AvailabilitySlot b : bSlots) {
                if (a.getDayOfWeek() == b.getDayOfWeek()
                        && a.getStartTime().isBefore(b.getEndTime())
                        && b.getStartTime().isBefore(a.getEndTime())) {
                    overlaps.add(a);
                }
            }
        }
        return overlaps;
    }
}
