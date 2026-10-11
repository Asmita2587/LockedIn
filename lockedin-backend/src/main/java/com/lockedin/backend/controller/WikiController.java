package com.lockedin.backend.controller;

import com.lockedin.backend.entity.GroupWikiEntry;
import com.lockedin.backend.service.WikiService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups/{groupId}/wiki")
public class WikiController {

    private final WikiService wikiService;

    public WikiController(WikiService wikiService) {
        this.wikiService = wikiService;
    }

    @PostMapping
    public GroupWikiEntry append(
            @PathVariable Long groupId,
            @RequestParam Long topicId,
            @RequestParam Long sessionId,
            @RequestParam String notes
    ) {
        return wikiService.appendSessionNotes(groupId, topicId, sessionId, notes);
    }

    @GetMapping
    public List<GroupWikiEntry> getWiki(@PathVariable Long groupId) {
        return wikiService.getWikiForGroup(groupId);
    }
}
