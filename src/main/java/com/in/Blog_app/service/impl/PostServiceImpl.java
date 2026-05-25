package com.in.Blog_app.service.impl;

import com.in.Blog_app.exception.ResourceNotFoundException;

import com.in.Blog_app.dto.PostDto;
import com.in.Blog_app.dto.PostRequest;
import com.in.Blog_app.entity.Post;
import com.in.Blog_app.entity.User;
import com.in.Blog_app.repository.PostRepository;
import com.in.Blog_app.repository.UserRepository;
import com.in.Blog_app.service.PostService;
import com.in.Blog_app.util.InputSanitizer;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final InputSanitizer inputSanitizer;

    @Override
    public PostDto createPost(PostRequest postRequest, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Post post = new Post();
        post.setTitle(inputSanitizer.sanitizeSingleLineText(postRequest.getTitle(), 200, "title"));
        post.setContent(inputSanitizer.sanitizeMultilineText(postRequest.getContent(), 5000000, "content"));
        post.setAuthor(user);

        Post savedPost = postRepository.save(post);
        return modelMapper.map(savedPost, PostDto.class);
    }

    @Override
    public PostDto updatePost(PostRequest postRequest, Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

        post.setTitle(inputSanitizer.sanitizeSingleLineText(postRequest.getTitle(), 200, "title"));
        post.setContent(inputSanitizer.sanitizeMultilineText(postRequest.getContent(), 5000000, "content"));

        Post updatedPost = postRepository.save(post);
        return modelMapper.map(updatedPost, PostDto.class);
    }

    @Override
    public void deletePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
        postRepository.delete(post);
    }

    @Override
    public PostDto getPostById(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
        return modelMapper.map(post, PostDto.class);
    }

    @Override
    public Page<PostDto> getAllPosts(Pageable pageable) {
        Page<Post> posts = postRepository.findAll(pageable);
        return posts.map(post -> modelMapper.map(post, PostDto.class));
    }

    @Override
    public Page<PostDto> getMyPosts(Long userId, Pageable pageable) {
        Page<Post> posts = postRepository.findByAuthorId(userId, pageable);
        return posts.map(post -> modelMapper.map(post, PostDto.class));
    }
}
