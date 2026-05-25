package com.in.Blog_app.controller;

import com.in.Blog_app.entity.RoleName;
import java.util.Arrays;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<String> getRoles() {
        return Arrays.stream(RoleName.values())
                .map(Enum::name)
                .toList();
    }
}
