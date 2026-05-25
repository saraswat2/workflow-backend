package com.in.Blog_app.controller;

import com.in.Blog_app.dto.PostDto;
import com.in.Blog_app.dto.engagement.EngagementSummaryDto;
import com.in.Blog_app.dto.engagement.ReactionRequest;
import com.in.Blog_app.security.UserPrincipal;
import com.in.Blog_app.service.EngagementService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Validated
public class EngagementController {

    private final EngagementService engagementService;

    /** GET /api/posts/{postId}/engagement — summary for current user */
    @GetMapping("/{postId}/engagement")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EngagementSummaryDto> getSummary(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(engagementService.getEngagementSummary(postId, user.getId()));
    }

    /** POST /api/posts/{postId}/like — toggle like */
    @PostMapping("/{postId}/like")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<EngagementSummaryDto> toggleLike(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(engagementService.toggleLike(postId, user.getId()));
    }

    /** POST /api/posts/{postId}/bookmark — toggle bookmark */
    @PostMapping("/{postId}/bookmark")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<EngagementSummaryDto> toggleBookmark(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(engagementService.toggleBookmark(postId, user.getId()));
    }

    /** GET /api/posts/bookmarks — current user's bookmarked posts */
    @GetMapping("/bookmarks")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Page<PostDto>> getBookmarks(
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size,
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(engagementService.getBookmarkedPosts(user.getId(), PageRequest.of(page, size)));
    }

    /** POST /api/posts/{postId}/view — record a view */
    @PostMapping("/{postId}/view")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EngagementSummaryDto> recordView(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(engagementService.recordView(postId, user.getId()));
    }

    /** POST /api/posts/{postId}/share — record a share */
    @PostMapping("/{postId}/share")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EngagementSummaryDto> recordShare(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(engagementService.recordShare(postId, user.getId()));
    }

    /** POST /api/posts/{postId}/reaction — add or update emoji reaction */
    @PostMapping("/{postId}/reaction")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<EngagementSummaryDto> react(
            @PathVariable Long postId,
            @Valid @RequestBody ReactionRequest request,
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(engagementService.reactToPost(postId, user.getId(), request.getReactionType()));
    }

    /** DELETE /api/posts/{postId}/reaction — remove emoji reaction */
    @DeleteMapping("/{postId}/reaction")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<EngagementSummaryDto> removeReaction(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(engagementService.removeReaction(postId, user.getId()));
    }
}
