package com.in.Blog_app.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = false)
public class CommentRequest {

    @NotBlank(message = "Content is required")
    @Size(max = 2000, message = "Comment must be at most 2000 characters")
    private String content;
}
