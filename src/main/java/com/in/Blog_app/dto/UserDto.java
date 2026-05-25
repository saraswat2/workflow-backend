package com.in.Blog_app.dto;

import lombok.Data;
import java.util.Set;

@Data
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private String name;
    private String profileImageUrl;
    private Set<String> roles;
}
