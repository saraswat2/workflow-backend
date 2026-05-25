package com.in.Blog_app.dto.auth;

import java.util.Set;

public record JwtResponse(
        String token,
        String type,
        Long id,
        String username,
        String email,
        Set<String> roles
) {
}
