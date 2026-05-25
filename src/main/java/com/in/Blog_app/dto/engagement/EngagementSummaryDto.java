package com.in.Blog_app.dto.engagement;

import com.in.Blog_app.entity.ReactionType;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class EngagementSummaryDto {
    private long likeCount;
    private long viewCount;
    private long shareCount;
    private boolean likedByCurrentUser;
    private boolean bookmarkedByCurrentUser;
    private Map<ReactionType, Long> reactionCounts;
    private ReactionType currentUserReaction; // null if no reaction
}
