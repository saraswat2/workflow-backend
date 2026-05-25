package com.in.Blog_app.service;

import com.in.Blog_app.dto.auth.JwtResponse;
import com.in.Blog_app.dto.auth.LoginRequest;
import com.in.Blog_app.dto.auth.MessageResponse;
import com.in.Blog_app.dto.auth.RegisterRequest;
import com.in.Blog_app.entity.Role;
import com.in.Blog_app.entity.RoleName;
import com.in.Blog_app.entity.User;
import com.in.Blog_app.repository.RoleRepository;
import com.in.Blog_app.repository.UserRepository;
import com.in.Blog_app.security.JwtUtils;
import com.in.Blog_app.security.UserPrincipal;
import com.in.Blog_app.util.InputSanitizer;
import jakarta.transaction.Transactional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final InputSanitizer inputSanitizer;

    public JwtResponse authenticate(LoginRequest loginRequest) {
        String usernameOrEmail = inputSanitizer.sanitizeLoginIdentifier(loginRequest.usernameOrEmail());
        inputSanitizer.validatePassword(loginRequest.password());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(usernameOrEmail, loginRequest.password())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Set<String> roles = userPrincipal.getAuthorities().stream()
                    .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                    .collect(java.util.stream.Collectors.toSet());

            return new JwtResponse(
                    jwt,
                    "Bearer",
                    userPrincipal.getId(),
                    userPrincipal.getUsername(),
                    userPrincipal.getEmail(),
                    roles
            );
        } catch (BadCredentialsException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username/email or password");
        }
    }

    @Transactional
    public MessageResponse register(RegisterRequest registerRequest) {
        String username = inputSanitizer.sanitizeUsername(registerRequest.username());
        String email = inputSanitizer.sanitizeEmail(registerRequest.email());
        String password = inputSanitizer.validatePassword(registerRequest.password());

        if (userRepository.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is already taken");
        }

        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is already in use");
        }

        Role userRole = roleRepository.findByName(RoleName.USER)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Default USER role is missing"));

        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(password))
                .roles(Set.of(userRole))
                .build();

        userRepository.save(user);
        return new MessageResponse("User registered successfully");
    }
}
