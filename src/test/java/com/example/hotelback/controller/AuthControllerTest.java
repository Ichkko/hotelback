package com.example.hotelback.controller;

import com.example.hotelback.exception.GlobalExceptionHandler;
import com.example.hotelback.exception.TooManyRequestsException;
import com.example.hotelback.model.User;
import com.example.hotelback.repository.UserRepository;
import com.example.hotelback.security.JwtUtil;
import com.example.hotelback.security.LoginAttemptService;
import com.example.hotelback.security.TokenBlacklistService;
import com.example.hotelback.service.GoogleAuthService;
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

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
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

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private LoginAttemptService loginAttemptService;

    @Mock
    private GoogleAuthService googleAuthService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        AuthController authController = new AuthController(
                authenticationManager,
                userRepository,
                passwordEncoder,
                jwtUtil,
                tokenBlacklistService,
                loginAttemptService,
                googleAuthService
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
        when(jwtUtil.generateAccessToken(any(User.class))).thenReturn("jwt-token");
        when(jwtUtil.generateRefreshToken(any(User.class))).thenReturn("refresh-token");

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
                .andExpect(jsonPath("$.email").value("admin-request@example.com"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getRole()).isEqualTo("USER");
        assertThat(savedUser.getPassword()).isEqualTo("encoded-secret");
        verify(jwtUtil).generateAccessToken(any(User.class));
        verify(jwtUtil).generateRefreshToken(any(User.class));
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

        verify(loginAttemptService).recordFailure("bad@example.com");
    }

    @Test
    void loginReturnsTooManyRequestsWhenRateLimited() throws Exception {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("bad credentials"));
        doThrow(new TooManyRequestsException(com.example.hotelback.exception.ErrorCode.AUTH_RATE_LIMITED,
                "Олон удаагийн буруу оролдлого илэрлээ. Түр хүлээгээд дахин оролдоно уу"))
                .when(loginAttemptService).recordFailure("blocked@example.com");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "blocked@example.com",
                                "password", "wrong-pass"
                        ))))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("AUTH_RATE_LIMITED"));
    }

    @Test
    void loginReturnsTokenPairForValidCredentials() throws Exception {
        User user = new User();
        user.setId(21L);
        user.setEmail("user@example.com");
        user.setName("Valid User");
        user.setRole("USER");

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(jwtUtil.generateAccessToken(any(User.class))).thenReturn("token-123");
        when(jwtUtil.generateRefreshToken(any(User.class))).thenReturn("refresh-123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "user@example.com",
                                "password", "secret123"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-123"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-123"))
                .andExpect(jsonPath("$.userId").value(21L))
                .andExpect(jsonPath("$.role").value("USER"));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(loginAttemptService).recordSuccess("user@example.com");
    }

    @Test
    void googleLoginCreatesUserWhenEmailDoesNotExist() throws Exception {
        when(googleAuthService.verify("google-id-token"))
                .thenReturn(new GoogleAuthService.GoogleUserInfo("google@example.com", "Google User"));
        when(userRepository.findByEmail("google@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(31L);
            return savedUser;
        });
        when(jwtUtil.generateAccessToken(any(User.class))).thenReturn("google-access");
        when(jwtUtil.generateRefreshToken(any(User.class))).thenReturn("google-refresh");

        mockMvc.perform(post("/api/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "idToken", "google-id-token"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("google-access"))
                .andExpect(jsonPath("$.refreshToken").value("google-refresh"))
                .andExpect(jsonPath("$.email").value("google@example.com"))
                .andExpect(jsonPath("$.role").value("USER"));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPassword()).isNull();
        assertThat(userCaptor.getValue().getGlobalRole()).isEqualTo(com.example.hotelback.model.GlobalRole.USER);
    }

    @Test
    void googleLoginUsesExistingUserWhenEmailExists() throws Exception {
        User existing = new User();
        existing.setId(41L);
        existing.setName("Existing User");
        existing.setEmail("existing@example.com");
        existing.setRole("USER");

        when(googleAuthService.verify("google-id-token"))
                .thenReturn(new GoogleAuthService.GoogleUserInfo("existing@example.com", "Google User"));
        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(existing));
        when(jwtUtil.generateAccessToken(existing)).thenReturn("existing-access");
        when(jwtUtil.generateRefreshToken(existing)).thenReturn("existing-refresh");

        mockMvc.perform(post("/api/auth/gmail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "idToken", "google-id-token"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("existing-access"))
                .andExpect(jsonPath("$.refreshToken").value("existing-refresh"))
                .andExpect(jsonPath("$.userId").value(41L));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void refreshReturnsNewTokenPairForValidRefreshToken() throws Exception {
        User user = new User();
        user.setId(21L);
        user.setEmail("user@example.com");
        user.setName("Valid User");
        user.setRole("USER");

        when(tokenBlacklistService.isBlacklisted("refresh-123")).thenReturn(false);
        when(jwtUtil.extractEmail("refresh-123")).thenReturn("user@example.com");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(jwtUtil.isRefreshTokenValid(eq("refresh-123"), any())).thenReturn(true);
        when(jwtUtil.extractExpiration("refresh-123")).thenReturn(Instant.parse("2026-04-01T00:00:00Z"));
        when(jwtUtil.generateAccessToken(any(User.class))).thenReturn("new-access");
        when(jwtUtil.generateRefreshToken(any(User.class))).thenReturn("new-refresh");

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "refreshToken", "refresh-123"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("new-access"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh"));

        verify(tokenBlacklistService).blacklist("refresh-123", Instant.parse("2026-04-01T00:00:00Z"));
    }

    @Test
    void logoutBlacklistsAccessAndRefreshTokens() throws Exception {
        when(jwtUtil.extractExpiration("access-123")).thenReturn(Instant.parse("2026-04-01T00:00:00Z"));
        when(jwtUtil.extractExpiration("refresh-123")).thenReturn(Instant.parse("2026-04-02T00:00:00Z"));

        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer access-123")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "refreshToken", "refresh-123"
                        ))))
                .andExpect(status().isNoContent());

        verify(tokenBlacklistService).blacklist("access-123", Instant.parse("2026-04-01T00:00:00Z"));
        verify(tokenBlacklistService).blacklist("refresh-123", Instant.parse("2026-04-02T00:00:00Z"));
    }
}
