package com.lockedin.backend.service;

import com.lockedin.backend.entity.*;
import com.lockedin.backend.repository.GroupMemberRepository;
import com.lockedin.backend.repository.SkillRepository;
import com.lockedin.backend.repository.StudyGroupRepository;
import com.lockedin.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class GroupService {

    private final StudyGroupRepository groupRepository;
    private final GroupMemberRepository memberRepository;
    private final SkillRepository skillRepository;
    private final UserRepository userRepository;

    public GroupService(StudyGroupRepository groupRepository,
                        GroupMemberRepository memberRepository,
                        SkillRepository skillRepository,
                        UserRepository userRepository) {
        this.groupRepository = groupRepository;
        this.memberRepository = memberRepository;
        this.skillRepository = skillRepository;
        this.userRepository = userRepository;
    }

    public StudyGroup createGroup(Long creatorId, String subjectName, LocalDate examDate) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new IllegalArgumentException("User not found. Please log in again."));

        String name = subjectName.trim();
        Skill subject = skillRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> skillRepository.save(Skill.builder().name(name).build()));

        StudyGroup group = StudyGroup.builder()
                .creator(creator)
                .subject(subject)
                .examDate(examDate)
                .build();
        group = groupRepository.save(group);

        memberRepository.save(GroupMember.builder()
                .group(group)
                .user(creator)
                .role(GroupRole.CREATOR)
                .build());

        return group;
    }

    public GroupMember joinGroup(Long groupId, Long userId) {
        StudyGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("No group found with Group ID " + groupId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found. Please log in again."));

        return memberRepository.findByGroupIdAndUserId(groupId, userId).orElseGet(() ->
                memberRepository.save(GroupMember.builder()
                        .group(group)
                        .user(user)
                        .role(GroupRole.MEMBER)
                        .build()));
    }

    public StudyGroup getGroup(Long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("No group found with Group ID " + groupId));
    }

    public List<StudyGroup> getMyGroups(Long userId) {
        return memberRepository.findByUserId(userId).stream().map(GroupMember::getGroup).toList();
    }

    public List<GroupMember> getMembers(Long groupId) {
        return memberRepository.findByGroupId(groupId);
    }
}
