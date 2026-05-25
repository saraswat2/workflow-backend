package com.in.Blog_app.service;

import com.in.Blog_app.dto.PostDto;
import com.in.Blog_app.dto.PostRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostService {
    PostDto createPost(PostRequest postRequest, Long userId);
    PostDto updatePost(PostRequest postRequest, Long postId, Long userId);
    void deletePost(Long postId, Long userId);
    PostDto getPostById(Long postId);
    Page<PostDto> getAllPosts(Pageable pageable);
    Page<PostDto> getMyPosts(Long userId, Pageable pageable);
}
