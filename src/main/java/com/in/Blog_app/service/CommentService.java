package com.in.Blog_app.service;

import com.in.Blog_app.dto.CommentDto;
import com.in.Blog_app.dto.CommentRequest;
import java.util.List;

public interface CommentService {
    CommentDto createComment(CommentRequest commentRequest, Long postId, Long userId);
    void deleteComment(Long commentId);
    List<CommentDto> getCommentsByPostId(Long postId);
}
