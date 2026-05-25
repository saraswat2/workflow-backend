package com.in.Blog_app.repository;

import com.in.Blog_app.entity.PostBookmark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostBookmarkRepository extends JpaRepository<PostBookmark, Long> {
    boolean existsByPostIdAndUserId(Long postId, Long userId);
    Optional<PostBookmark> findByPostIdAndUserId(Long postId, Long userId);
    Page<PostBookmark> findByUserId(Long userId, Pageable pageable);
}
