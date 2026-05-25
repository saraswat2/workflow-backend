package com.in.Blog_app.dto.user;

import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public record UpdateProfileRequest(
        @Size(max = 100, message = "Name must not exceed 100 characters")
        String name,
        
        String profileImageUrl
) {
}
