package com.in.Blog_app.dto.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.in.Blog_app.entity.RoleName;
import jakarta.validation.constraints.NotEmpty;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = false)
public record UpdateRolesRequest(
        @NotEmpty(message = "At least one role is required")
        Set<RoleName> roles
) {
}
