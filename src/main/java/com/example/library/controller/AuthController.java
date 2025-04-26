package com.example.library.controller;

import com.example.library.dto.AuthRequest;
import com.example.library.dto.AuthResponse;
import com.example.library.dto.TokenRefreshRequest;
import com.example.library.model.RefreshToken;
import com.example.library.model.User;
import com.example.library.repository.UserRepository;
import com.example.library.service.JwtService;
import com.example.library.service.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;

    private static final long ACCESS_TOKEN_EXPIRATION_MS = 15 * 60 * 1000;

    @Operation(
            summary = "User login",
            description = "Authenticates the user with the provided credentials (email and password) and generates an access token and refresh token. The access token is used for subsequent API calls while the refresh token is used to generate a new access token once it expires."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful, access token and refresh token returned",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized, invalid credentials",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"error\": \"Invalid credentials\"}")
                    )
            )
    })
    @PostMapping("/login")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN', 'USER')")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest authRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.email(), authRequest.password())
            );

            User user = userRepository.findByEmail(authRequest.email())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String accessToken = jwtService.generateToken(user);
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

            return ResponseEntity.ok(
                    new AuthResponse(accessToken, refreshToken.getToken().toString(), ACCESS_TOKEN_EXPIRATION_MS / 1000)
            );

        } catch (AuthenticationException ex) {
            return ResponseEntity.status(401).build();
        }
    }

    @Operation(
            summary = "Refresh access token",
            description = "Refreshes the user's access token using the provided refresh token. The refresh token is verified for expiration, and if valid, a new access token is issued."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Access token refreshed successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request, invalid refresh token",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"error\": \"Invalid refresh token\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden, refresh token expired or invalid",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"error\": \"Refresh token is expired or invalid\"}")
                    )
            )
    })
    @PostMapping("/refresh")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN', 'USER')")
    public ResponseEntity<AuthResponse> refresh(@RequestBody TokenRefreshRequest request) {
        try {
            UUID tokenId = UUID.fromString(request.refreshToken());
            RefreshToken refreshToken = refreshTokenService.findByToken(tokenId)
                    .map(refreshTokenService::verifyExpiration)
                    .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

            String accessToken = jwtService.generateToken(refreshToken.getUser());

            return ResponseEntity.ok(
                    new AuthResponse(accessToken, refreshToken.getToken().toString(), ACCESS_TOKEN_EXPIRATION_MS / 1000)
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).build();
        }
    }

    @Operation(
            summary = "Logout user",
            description = "Logs the user out by deleting their refresh token from the system. This ensures that no new access tokens can be generated using the expired refresh token."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Logout successful, refresh token deleted",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request, invalid refresh token",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"error\": \"Invalid refresh token\"}")
                    )
            )
    })
    @DeleteMapping("/logout")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN', 'USER')")
    public ResponseEntity<?> logout(@RequestBody TokenRefreshRequest request) {
        try {
            UUID tokenId = UUID.fromString(request.refreshToken());
            refreshTokenService.findByToken(tokenId).ifPresent(refreshTokenService::deleteToken);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

}
