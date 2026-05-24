package com.example.hotelback.controller;

import com.example.hotelback.dto.AuthResponse;
import com.example.hotelback.dto.GoogleAuthRequest;
import com.example.hotelback.dto.LoginRequest;
import com.example.hotelback.dto.LogoutRequest;
import com.example.hotelback.dto.RefreshTokenRequest;
import com.example.hotelback.dto.RegisterRequest;
import com.example.hotelback.exception.BadRequestException;
import com.example.hotelback.exception.ErrorCode;
import com.example.hotelback.exception.ResourceNotFoundException;
import com.example.hotelback.exception.UnauthorizedException;
import com.example.hotelback.model.GlobalRole;
import com.example.hotelback.model.User;
import com.example.hotelback.repository.UserRepository;
import com.example.hotelback.security.JwtUtil;
import com.example.hotelback.security.LoginAttemptService;
import com.example.hotelback.security.TokenBlacklistService;
import com.example.hotelback.service.GoogleAuthService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;
    private final LoginAttemptService loginAttemptService;
    private final GoogleAuthService googleAuthService;

    public AuthController(AuthenticationManager authenticationManager,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil,
                          TokenBlacklistService tokenBlacklistService,
                          LoginAttemptService loginAttemptService,
                          GoogleAuthService googleAuthService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.tokenBlacklistService = tokenBlacklistService;
        this.loginAttemptService = loginAttemptService;
        this.googleAuthService = googleAuthService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BadRequestException(ErrorCode.INVALID_REQUEST, "Энэ email бүртгэлтэй байна");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setGlobalRole(GlobalRole.USER);

        userRepository.save(user);

        return ResponseEntity.ok(buildAuthResponse(user));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        loginAttemptService.checkAllowed(request.getEmail());
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            loginAttemptService.recordSuccess(request.getEmail());
        } catch (BadCredentialsException e) {
            loginAttemptService.recordFailure(request.getEmail());
            throw new UnauthorizedException(ErrorCode.AUTH_INVALID_CREDENTIALS, "Email эсвэл нууц үг буруу");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Хэрэглэгч олдсонгүй"));

        return ResponseEntity.ok(buildAuthResponse(user));
    }

    @PostMapping({"/google", "/gmail"})
    public ResponseEntity<AuthResponse> googleLogin(@Valid @RequestBody GoogleAuthRequest request) {
        GoogleAuthService.GoogleUserInfo googleUser = googleAuthService.verify(request.getIdToken());
        User user = userRepository.findByEmail(googleUser.email())
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setName(googleUser.name());
                    newUser.setEmail(googleUser.email());
                    newUser.setGlobalRole(GlobalRole.USER);
                    return userRepository.save(newUser);
                });

        return ResponseEntity.ok(buildAuthResponse(user));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        if (tokenBlacklistService.isBlacklisted(refreshToken)) {
            throw new UnauthorizedException(ErrorCode.AUTH_INVALID_TOKEN, "Refresh token хүчингүй болсон байна");
        }

        try {
            String email = jwtUtil.extractEmail(refreshToken);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("Хэрэглэгч олдсонгүй"));
            UserDetails userDetails = org.springframework.security.core.userdetails.User
                    .withUsername(user.getEmail())
                    .password(user.getPassword() != null ? user.getPassword() : "")
                    .authorities("ROLE_" + (user.getGlobalRole() != null ? user.getGlobalRole().name() : GlobalRole.USER.name()))
                    .build();

            if (!jwtUtil.isRefreshTokenValid(refreshToken, userDetails)) {
                throw new UnauthorizedException(ErrorCode.AUTH_INVALID_TOKEN, "Refresh token буруу байна");
            }

            tokenBlacklistService.blacklist(refreshToken, jwtUtil.extractExpiration(refreshToken));
            return ResponseEntity.ok(buildAuthResponse(user));
        } catch (ExpiredJwtException e) {
            throw new UnauthorizedException(ErrorCode.AUTH_TOKEN_EXPIRED, "Refresh token-ийн хугацаа дууссан байна");
        } catch (JwtException | IllegalArgumentException e) {
            throw new UnauthorizedException(ErrorCode.AUTH_INVALID_TOKEN, "Refresh token буруу эсвэл эвдэрсэн байна");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody(required = false) LogoutRequest request,
                                       HttpServletRequest httpRequest) {
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String accessToken = authHeader.substring(7);
            tokenBlacklistService.blacklist(accessToken, safeExpiration(accessToken));
        }
        if (request != null && request.getRefreshToken() != null && !request.getRefreshToken().isBlank()) {
            tokenBlacklistService.blacklist(request.getRefreshToken(), safeExpiration(request.getRefreshToken()));
        }
        return ResponseEntity.noContent().build();
    }

    private java.time.Instant safeExpiration(String token) {
        try {
            return jwtUtil.extractExpiration(token);
        } catch (Exception ex) {
            return java.time.Instant.now().plusSeconds(3600);
        }
    }

    private AuthResponse buildAuthResponse(User user) {
        GlobalRole globalRole = user.getGlobalRole() != null ? user.getGlobalRole() : GlobalRole.USER;
        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);
        return new AuthResponse(accessToken, refreshToken, user.getId(), user.getEmail(), user.getName(), globalRole.name());
    }
}
