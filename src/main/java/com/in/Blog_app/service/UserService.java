package com.in.Blog_app.service;

import com.in.Blog_app.dto.user.UpdateRolesRequest;
import com.in.Blog_app.dto.user.UserResponse;
import com.in.Blog_app.entity.Role;
import com.in.Blog_app.entity.RoleName;
import com.in.Blog_app.entity.User;
import com.in.Blog_app.repository.RoleRepository;
import com.in.Blog_app.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Transactional
    public UserResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No authenticated user found");
        }

        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Authenticated user not found"));

        return mapUser(user);
    }

    @Transactional
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .sorted(Comparator.comparing(User::getId))
                .map(this::mapUser)
                .toList();
    }

    @Transactional
    public UserResponse updateUserRoles(Long userId, UpdateRolesRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Set<Role> roles = request.roles().stream()
                .map(this::resolveRole)
                .collect(Collectors.toSet());

        user.setRoles(roles);
        return mapUser(userRepository.save(user));
    }

    @Transactional
    public UserResponse updateProfile(com.in.Blog_app.dto.user.UpdateProfileRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No authenticated user found");
        }

        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Authenticated user not found"));

        if (request.name() != null && !request.name().trim().isEmpty()) {
            user.setName(request.name().trim());
        }
        
        if (request.profileImageUrl() != null) {
            if (request.profileImageUrl().trim().isEmpty()) {
                user.setProfileImageUrl(null);
            } else {
                user.setProfileImageUrl(request.profileImageUrl());
            }
        }

        return mapUser(userRepository.save(user));
    }

    private Role resolveRole(RoleName roleName) {
        return roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found: " + roleName));
    }

    private UserResponse mapUser(User user) {
        Set<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toCollection(java.util.TreeSet::new));

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getName(),
                user.getProfileImageUrl(),
                Boolean.TRUE.equals(user.getEnabled()),
                roles
        );
    }
}
