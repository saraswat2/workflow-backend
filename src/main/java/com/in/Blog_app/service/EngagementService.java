package com.in.Blog_app.service;

import com.in.Blog_app.dto.PostDto;
import com.in.Blog_app.dto.engagement.EngagementSummaryDto;
import com.in.Blog_app.entity.ReactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EngagementService {

    // Likes
    EngagementSummaryDto toggleLike(Long postId, Long userId);

    // Bookmarks
    EngagementSummaryDto toggleBookmark(Long postId, Long userId);
    Page<PostDto> getBookmarkedPosts(Long userId, Pageable pageable);

    // Views
    EngagementSummaryDto recordView(Long postId, Long userId);

    // Shares
    EngagementSummaryDto recordShare(Long postId, Long userId);

    // Reactions
    EngagementSummaryDto reactToPost(Long postId, Long userId, ReactionType reactionType);
    EngagementSummaryDto removeReaction(Long postId, Long userId);

    // Summary
    EngagementSummaryDto getEngagementSummary(Long postId, Long userId);
}
