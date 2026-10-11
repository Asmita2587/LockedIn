package com.lockedin.backend.controller;

import com.lockedin.backend.entity.AvailabilitySlot;
import com.lockedin.backend.service.AvailabilityService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/availability")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    public AvailabilityController(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @PostMapping("/me")
    public AvailabilitySlot addSlot(
            @AuthenticationPrincipal Long userId,
            @RequestParam DayOfWeek day,
            @RequestParam LocalTime start,
            @RequestParam LocalTime end
    ) {
        return availabilityService.addSlot(userId, day, start, end);
    }

    @GetMapping("/me")
    public List<AvailabilitySlot> mySlots(@AuthenticationPrincipal Long userId) {
        return availabilityService.getSlots(userId);
    }

    @GetMapping("/overlap")
    public List<AvailabilitySlot> overlap(@RequestParam Long userAId, @RequestParam Long userBId) {
        return availabilityService.findOverlap(userAId, userBId);
    }
}
