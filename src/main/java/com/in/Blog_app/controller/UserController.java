package com.in.Blog_app.controller;

import com.in.Blog_app.dto.user.UpdateRolesRequest;
import com.in.Blog_app.dto.user.UserResponse;
import com.in.Blog_app.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@Validated
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public UserResponse getCurrentUser() {
        return userService.getCurrentUser();
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers();
    }

    @PatchMapping("/{userId}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse updateUserRoles(@PathVariable @Positive Long userId, @Valid @RequestBody UpdateRolesRequest request) {
        return userService.updateUserRoles(userId, request);
    }

    @PatchMapping("/me")
    public UserResponse updateProfile(@Valid @RequestBody com.in.Blog_app.dto.user.UpdateProfileRequest request) {
        return userService.updateProfile(request);
    }
}
