package com.in.Blog_app.controller;

import com.in.Blog_app.entity.User;
import com.in.Blog_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TestController {
    private final UserRepository userRepository;

    @GetMapping("/api/test-batman")
    public String getBatman() {
        User u = userRepository.findByUsername("Batman15").orElse(null);
        if (u == null) return "Not found";
        return "Profile URL: " + u.getProfileImageUrl();
    }
}
