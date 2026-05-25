package com.in.Blog_app.service.impl;

import com.in.Blog_app.dto.PostDto;
import com.in.Blog_app.dto.engagement.EngagementSummaryDto;
import com.in.Blog_app.entity.*;
import com.in.Blog_app.repository.*;
import com.in.Blog_app.service.EngagementService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EngagementServiceImpl implements EngagementService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostBookmarkRepository postBookmarkRepository;
    private final PostReactionRepository postReactionRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public EngagementSummaryDto toggleLike(Long postId, Long userId) {
        Post post = getPost(postId);
        Optional<PostLike> existing = postLikeRepository.findByPostIdAndUserId(postId, userId);
        if (existing.isPresent()) {
            postLikeRepository.delete(existing.get());
        } else {
            User user = getUser(userId);
            postLikeRepository.save(PostLike.builder().post(post).user(user).build());
        }
        return buildSummary(post, userId);
    }

    @Override
    @Transactional
    public EngagementSummaryDto toggleBookmark(Long postId, Long userId) {
        Post post = getPost(postId);
        Optional<PostBookmark> existing = postBookmarkRepository.findByPostIdAndUserId(postId, userId);
        if (existing.isPresent()) {
            postBookmarkRepository.delete(existing.get());
        } else {
            User user = getUser(userId);
            postBookmarkRepository.save(PostBookmark.builder().post(post).user(user).build());
        }
        return buildSummary(post, userId);
    }

    @Override
    public Page<PostDto> getBookmarkedPosts(Long userId, Pageable pageable) {
        return postBookmarkRepository.findByUserId(userId, pageable)
                .map(bookmark -> modelMapper.map(bookmark.getPost(), PostDto.class));
    }

    @Override
    @Transactional
    public EngagementSummaryDto recordView(Long postId, Long userId) {
        Post post = getPost(postId);
        post.setViewCount(post.getViewCount() + 1);
        postRepository.save(post);
        return buildSummary(post, userId);
    }

    @Override
    @Transactional
    public EngagementSummaryDto recordShare(Long postId, Long userId) {
        Post post = getPost(postId);
        post.setShareCount(post.getShareCount() + 1);
        postRepository.save(post);
        return buildSummary(post, userId);
    }

    @Override
    @Transactional
    public EngagementSummaryDto reactToPost(Long postId, Long userId, ReactionType reactionType) {
        Post post = getPost(postId);
        Optional<PostReaction> existing = postReactionRepository.findByPostIdAndUserId(postId, userId);
        if (existing.isPresent()) {
            existing.get().setReactionType(reactionType);
            postReactionRepository.save(existing.get());
        } else {
            User user = getUser(userId);
            postReactionRepository.save(PostReaction.builder().post(post).user(user).reactionType(reactionType).build());
        }
        return buildSummary(post, userId);
    }

    @Override
    @Transactional
    public EngagementSummaryDto removeReaction(Long postId, Long userId) {
        Post post = getPost(postId);
        postReactionRepository.findByPostIdAndUserId(postId, userId)
                .ifPresent(postReactionRepository::delete);
        return buildSummary(post, userId);
    }

    @Override
    public EngagementSummaryDto getEngagementSummary(Long postId, Long userId) {
        Post post = getPost(postId);
        return buildSummary(post, userId);
    }

    // --- helpers ---

    private Post getPost(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private EngagementSummaryDto buildSummary(Post post, Long userId) {
        long likeCount = postLikeRepository.countByPostId(post.getId());
        boolean liked = postLikeRepository.existsByPostIdAndUserId(post.getId(), userId);
        boolean bookmarked = postBookmarkRepository.existsByPostIdAndUserId(post.getId(), userId);

        List<Object[]> rawCounts = postReactionRepository.countByPostIdGroupByType(post.getId());
        Map<ReactionType, Long> reactionCounts = new EnumMap<>(ReactionType.class);
        for (Object[] row : rawCounts) {
            reactionCounts.put((ReactionType) row[0], (Long) row[1]);
        }

        ReactionType currentUserReaction = postReactionRepository
                .findByPostIdAndUserId(post.getId(), userId)
                .map(PostReaction::getReactionType)
                .orElse(null);

        return EngagementSummaryDto.builder()
                .likeCount(likeCount)
                .viewCount(post.getViewCount())
                .shareCount(post.getShareCount())
                .likedByCurrentUser(liked)
                .bookmarkedByCurrentUser(bookmarked)
                .reactionCounts(reactionCounts)
                .currentUserReaction(currentUserReaction)
                .build();
    }
}
