package com.example.hotelback.controller;

import com.example.hotelback.exception.GlobalExceptionHandler;
import com.example.hotelback.model.User;
import com.example.hotelback.repository.UserRepository;
import com.example.hotelback.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        AuthController authController = new AuthController(
                authenticationManager,
                userRepository,
                passwordEncoder,
                jwtUtil
        );

        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void registerIgnoresAdminRoleInputAndCreatesUserRole() throws Exception {
        when(userRepository.findByEmail("admin-request@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("secret123")).thenReturn("encoded-secret");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(10L);
            return savedUser;
        });
        when(jwtUtil.generateToken("admin-request@example.com", "USER")).thenReturn("jwt-token");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "New User",
                                "email", "admin-request@example.com",
                                "password", "secret123",
                                "phone", "99112233",
                                "role", "ADMIN",
                                "userRole", "ADMIN"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.email").value("admin-request@example.com"));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getRole()).isEqualTo("USER");
        assertThat(savedUser.getPassword()).isEqualTo("encoded-secret");
        verify(jwtUtil).generateToken(eq("admin-request@example.com"), eq("USER"));
    }

    @Test
    void registerReturnsBadRequestWhenEmailAlreadyExists() throws Exception {
        User existing = new User();
        existing.setId(1L);
        when(userRepository.findByEmail("duplicate@example.com")).thenReturn(Optional.of(existing));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Existing User",
                                "email", "duplicate@example.com",
                                "password", "secret123"
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Энэ email бүртгэлтэй байна"))
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.path").value("/api/auth/register"));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void loginReturnsUnauthorizedForBadCredentials() throws Exception {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "bad@example.com",
                                "password", "wrong-pass"
                        ))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Email эсвэл нууц үг буруу"))
                .andExpect(jsonPath("$.code").value("AUTH_INVALID_CREDENTIALS"))
                .andExpect(jsonPath("$.path").value("/api/auth/login"));
    }

    @Test
    void loginReturnsTokenForValidCredentials() throws Exception {
        User user = new User();
        user.setId(21L);
        user.setEmail("user@example.com");
        user.setName("Valid User");
        user.setRole("USER");

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken("user@example.com", "USER")).thenReturn("token-123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "user@example.com",
                                "password", "secret123"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-123"))
                .andExpect(jsonPath("$.userId").value(21L))
                .andExpect(jsonPath("$.role").value("USER"));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }
}
