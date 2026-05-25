package com.in.Blog_app.dto.user;

import java.util.Set;

public record UserResponse(
        Long id,
        String username,
        String email,
        String name,
        String profileImageUrl,
        boolean enabled,
        Set<String> roles
) {
}
