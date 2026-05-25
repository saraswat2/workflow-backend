package com.in.Blog_app.controller;

import com.in.Blog_app.dto.auth.JwtResponse;
import com.in.Blog_app.dto.auth.LoginRequest;
import com.in.Blog_app.dto.auth.MessageResponse;
import com.in.Blog_app.dto.auth.RegisterRequest;
import com.in.Blog_app.security.ratelimit.RateLimitService;
import com.in.Blog_app.service.AuthService;
import com.in.Blog_app.util.ClientRequestUtils;
import com.in.Blog_app.util.InputSanitizer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Validated
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RateLimitService rateLimitService;
    private final InputSanitizer inputSanitizer;

    @Value("${blog.app.security.rate-limit.login.ip.max-requests:20}")
    private int loginIpMaxRequests;

    @Value("${blog.app.security.rate-limit.login.ip.window-seconds:900}")
    private long loginIpWindowSeconds;

    @Value("${blog.app.security.rate-limit.login.user.max-requests:10}")
    private int loginUserMaxRequests;

    @Value("${blog.app.security.rate-limit.login.user.window-seconds:900}")
    private long loginUserWindowSeconds;

    @Value("${blog.app.security.rate-limit.register.ip.max-requests:10}")
    private int registerIpMaxRequests;

    @Value("${blog.app.security.rate-limit.register.ip.window-seconds:3600}")
    private long registerIpWindowSeconds;

    @Value("${blog.app.security.rate-limit.register.user.max-requests:5}")
    private int registerUserMaxRequests;

    @Value("${blog.app.security.rate-limit.register.user.window-seconds:3600}")
    private long registerUserWindowSeconds;

    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterRequest request, HttpServletRequest servletRequest) {
        enforceRegisterRateLimits(servletRequest, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        enforceLoginRateLimits(servletRequest, request);
        return ResponseEntity.ok(authService.authenticate(request));
    }

    /**
     * Apply both IP and account-identifier throttles to slow brute force and credential stuffing attacks.
     */
    private void enforceLoginRateLimits(HttpServletRequest servletRequest, LoginRequest request) {
        String clientIp = ClientRequestUtils.extractClientIp(servletRequest);
        String identifier = inputSanitizer.sanitizeLoginIdentifier(request.usernameOrEmail());

        rateLimitService.checkLimit(
                "public:login:ip:" + clientIp,
                loginIpMaxRequests,
                Duration.ofSeconds(loginIpWindowSeconds),
                "Too many login attempts from this IP. Please try again later."
        );
        rateLimitService.checkLimit(
                "public:login:user:" + identifier,
                loginUserMaxRequests,
                Duration.ofSeconds(loginUserWindowSeconds),
                "Too many login attempts for this account. Please try again later."
        );
    }

    private void enforceRegisterRateLimits(HttpServletRequest servletRequest, RegisterRequest request) {
        String clientIp = ClientRequestUtils.extractClientIp(servletRequest);
        String registrationKey = inputSanitizer.sanitizeUsername(request.username()) + ":" + inputSanitizer.sanitizeEmail(request.email());

        rateLimitService.checkLimit(
                "public:register:ip:" + clientIp,
                registerIpMaxRequests,
                Duration.ofSeconds(registerIpWindowSeconds),
                "Too many registration attempts from this IP. Please try again later."
        );
        rateLimitService.checkLimit(
                "public:register:user:" + registrationKey,
                registerUserMaxRequests,
                Duration.ofSeconds(registerUserWindowSeconds),
                "Too many registration attempts for this identity. Please try again later."
        );
    }
}
