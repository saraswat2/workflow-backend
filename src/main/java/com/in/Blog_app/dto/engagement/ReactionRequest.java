package com.in.Blog_app.dto.engagement;

import com.in.Blog_app.entity.ReactionType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReactionRequest {
    @NotNull(message = "Reaction type is required")
    private ReactionType reactionType;
}
