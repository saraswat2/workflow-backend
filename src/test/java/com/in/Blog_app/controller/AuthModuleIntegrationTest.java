package com.in.Blog_app.controller;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.in.Blog_app.entity.Role;
import com.in.Blog_app.entity.RoleName;
import com.in.Blog_app.entity.User;
import com.in.Blog_app.repository.RoleRepository;
import com.in.Blog_app.repository.UserRepository;
import com.in.Blog_app.security.ratelimit.RateLimitService;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class AuthModuleIntegrationTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final RateLimitService rateLimitService;

    @BeforeEach
    void cleanUsers() {
        userRepository.deleteAll();
        rateLimitService.clear();
    }

    @Test
    void registerCreatesUserWithDefaultUserRole() throws Exception {
        String payload = """
                {
                  \"username\": \"shivam\",
                  \"email\": \"shivam@example.com\",
                  \"password\": \"StrongPass123\"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
                        .header("X-Forwarded-For", "10.0.0.1"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("User registered successfully"));

        User savedUser = userRepository.findByUsername("shivam").orElseThrow();
        org.assertj.core.api.Assertions.assertThat(savedUser.getRoles())
                .extracting(role -> role.getName().name())
                .containsExactly("USER");
    }

    @Test
    void loginReturnsJwtTokenAndGrantedRoles() throws Exception {
        createUser("rahul", "rahul@example.com", "StrongPass123", Set.of(RoleName.USER));

        String payload = """
                {
                  \"usernameOrEmail\": \"rahul\",
                  \"password\": \"StrongPass123\"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
                        .header("X-Forwarded-For", "10.0.0.2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.username").value("rahul"))
                .andExpect(jsonPath("$.roles", containsInAnyOrder("USER")));
    }

    @Test
    void registerRejectsUnexpectedFields() throws Exception {
        String payload = """
                {
                  \"username\": \"unexpected\",
                  \"email\": \"unexpected@example.com\",
                  \"password\": \"StrongPass123\",
                  \"role\": \"ADMIN\"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
                        .header("X-Forwarded-For", "10.0.0.3"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Unexpected field: role")));
    }

    @Test
    void loginIsRateLimitedPerUser() throws Exception {
        createUser("lockeduser", "locked@example.com", "StrongPass123", Set.of(RoleName.USER));

        String payload = objectMapper.createObjectNode()
                .put("usernameOrEmail", "lockeduser")
                .put("password", "StrongPass123")
                .toString();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
                        .header("X-Forwarded-For", "10.0.0.4"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
                        .header("X-Forwarded-For", "10.0.0.4"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
                        .header("X-Forwarded-For", "10.0.0.4"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"))
                .andExpect(jsonPath("$.message", containsString("Too many login attempts")));
    }

    @Test
    void registerIsRateLimitedPerIp() throws Exception {
        String payload1 = objectMapper.createObjectNode()
                .put("username", "userone")
                .put("email", "userone@example.com")
                .put("password", "StrongPass123")
                .toString();
        String payload2 = objectMapper.createObjectNode()
                .put("username", "usertwo")
                .put("email", "usertwo@example.com")
                .put("password", "StrongPass123")
                .toString();
        String payload3 = objectMapper.createObjectNode()
                .put("username", "userthree")
                .put("email", "userthree@example.com")
                .put("password", "StrongPass123")
                .toString();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload1)
                        .header("X-Forwarded-For", "10.0.0.5"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload2)
                        .header("X-Forwarded-For", "10.0.0.5"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload3)
                        .header("X-Forwarded-For", "10.0.0.5"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"))
                .andExpect(jsonPath("$.message", containsString("Too many registration attempts")));
    }

    @Test
    void roleEndpointRequiresAdminRole() throws Exception {
        createUser("member", "member@example.com", "StrongPass123", Set.of(RoleName.USER));
        createUser("admin", "admin@example.com", "StrongPass123", Set.of(RoleName.ADMIN));

        String memberToken = loginAndExtractToken("member", "StrongPass123", "10.0.0.6");
        String adminToken = loginAndExtractToken("admin", "StrongPass123", "10.0.0.7");

        mockMvc.perform(get("/api/roles")
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/roles")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", containsInAnyOrder("USER", "ADMIN")));
    }

    private void createUser(String username, String email, String rawPassword, Set<RoleName> roles) {
        Set<Role> resolvedRoles = roles.stream()
                .map(roleName -> roleRepository.findByName(roleName).orElseThrow())
                .collect(java.util.stream.Collectors.toSet());

        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .roles(resolvedRoles)
                .build();

        userRepository.save(user);
    }

    private String loginAndExtractToken(String usernameOrEmail, String password, String forwardedFor) throws Exception {
        String payload = objectMapper.createObjectNode()
                .put("usernameOrEmail", usernameOrEmail)
                .put("password", password)
                .toString();

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
                        .header("X-Forwarded-For", forwardedFor))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode responseJson = objectMapper.readTree(result.getResponse().getContentAsString());
        return responseJson.get("token").asText();
    }
}
