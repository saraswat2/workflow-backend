package com.in.Blog_app.service.impl;

import com.in.Blog_app.dto.CommentDto;
import com.in.Blog_app.dto.CommentRequest;
import com.in.Blog_app.entity.Comment;
import com.in.Blog_app.entity.Post;
import com.in.Blog_app.entity.User;
import com.in.Blog_app.repository.CommentRepository;
import com.in.Blog_app.repository.PostRepository;
import com.in.Blog_app.repository.UserRepository;
import com.in.Blog_app.service.CommentService;
import com.in.Blog_app.util.InputSanitizer;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final InputSanitizer inputSanitizer;

    @Override
    public CommentDto createComment(CommentRequest commentRequest, Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Comment comment = new Comment();
        comment.setContent(inputSanitizer.sanitizeMultilineText(commentRequest.getContent(), 2000, "content"));
        comment.setPost(post);
        comment.setUser(user);

        Comment savedComment = commentRepository.save(comment);
        return modelMapper.map(savedComment, CommentDto.class);
    }

    @Override
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        commentRepository.delete(comment);
    }

    @Override
    public List<CommentDto> getCommentsByPostId(Long postId) {
        List<Comment> comments = commentRepository.findByPostId(postId);
        return comments.stream()
                .map(comment -> modelMapper.map(comment, CommentDto.class))
                .collect(Collectors.toList());
    }
}
