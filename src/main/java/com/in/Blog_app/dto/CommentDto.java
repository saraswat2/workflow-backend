package com.in.Blog_app.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = false)
public class CommentDto {
    @Null(message = "id is managed by the server")
    private Long id;

    @NotBlank(message = "content is required")
    @Size(max = 2000, message = "content must be at most 2000 characters")
    private String content;

    @Null(message = "user is managed by the server")
    private UserDto user;

    @Null(message = "createdAt is managed by the server")
    private LocalDateTime createdAt;
}
