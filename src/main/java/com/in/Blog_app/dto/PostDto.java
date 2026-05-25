package com.in.Blog_app.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = false)
public class PostDto {
    @Null(message = "id is managed by the server")
    private Long id;

    @NotBlank(message = "title is required")
    @Size(max = 200, message = "title must be at most 200 characters")
    private String title;

    @NotBlank(message = "content is required")
    @Size(max = 5000000, message = "content must be at most 5000000 characters")
    private String content;

    @Null(message = "author is managed by the server")
    private UserDto author;

    @Null(message = "createdAt is managed by the server")
    private LocalDateTime createdAt;

    @Null(message = "updatedAt is managed by the server")
    private LocalDateTime updatedAt;

    private long viewCount;
    private long shareCount;
}
