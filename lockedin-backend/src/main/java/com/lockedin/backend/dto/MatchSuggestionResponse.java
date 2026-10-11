package com.lockedin.backend.dto;

import com.lockedin.backend.entity.MatchType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MatchSuggestionResponse {
    private Long candidateUserId;
    private String candidateName;
    private Long skillId;
    private String skillName;
    private MatchType matchType;
}
