package com.lockedin.backend.controller;

import com.lockedin.backend.entity.GroupMember;
import com.lockedin.backend.entity.StudyGroup;
import com.lockedin.backend.service.GroupService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @PostMapping
    public StudyGroup create(
            @AuthenticationPrincipal Long userId,
            @RequestParam String subjectName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate examDate
    ) {
        return groupService.createGroup(userId, subjectName, examDate);
    }

    @GetMapping
    public List<StudyGroup> myGroups(@AuthenticationPrincipal Long userId) {
        return groupService.getMyGroups(userId);
    }

    @GetMapping("/{groupId}")
    public StudyGroup get(@PathVariable Long groupId) {
        return groupService.getGroup(groupId);
    }

    @PostMapping("/{groupId}/join")
    public GroupMember join(@AuthenticationPrincipal Long userId, @PathVariable Long groupId) {
        return groupService.joinGroup(groupId, userId);
    }

    @GetMapping("/{groupId}/members")
    public List<GroupMember> members(@PathVariable Long groupId) {
        return groupService.getMembers(groupId);
    }
}
