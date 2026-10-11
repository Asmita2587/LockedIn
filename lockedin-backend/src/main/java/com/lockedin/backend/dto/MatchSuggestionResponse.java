package com.lockedin.backend.dto;

import com.lockedin.backend.entity.MatchType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchSuggestionResponse {
    private Long candidateUserId;
    private String candidateName;
    private Long skillId;
    private String skillName;
    private MatchType matchType;
    private String proficiency;
    private String direction; // "LEARN" (they teach what you want to learn) or "TEACH" (they want to learn what you teach)

    public MatchSuggestionResponse(Long candidateUserId, String candidateName, Long skillId, String skillName, MatchType matchType) {
        this(candidateUserId, candidateName, skillId, skillName, matchType, "INTERMEDIATE", "LEARN");
    }
}
