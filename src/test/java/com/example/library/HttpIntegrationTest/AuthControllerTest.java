package com.example.library.HttpIntegrationTest;

import com.example.library.component.JwtAuthenticationFilter;
import com.example.library.controller.AuthController;
import com.example.library.dto.AuthRequest;
import com.example.library.dto.TokenRefreshRequest;
import com.example.library.model.RefreshToken;
import com.example.library.model.User;
import com.example.library.model.UserRole;
import com.example.library.repository.UserRepository;
import com.example.library.service.JwtService;
import com.example.library.service.RefreshTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(username = "admin", roles = "ADMIN")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;


    @MockitoBean
    AuthenticationManager authenticationManager;
    @MockitoBean
    JwtService jwtService;
    @MockitoBean
    RefreshTokenService refreshTokenService;
    @MockitoBean
    UserRepository userRepository;

    @MockitoBean
    JwtAuthenticationFilter jwtAuthenticationFilter;

    /* ---------- POST /auth/login ---------- */

    @Test
    void login_ok() throws Exception {
        User user = User.builder()
                .id(1L)
                .email("user@example.com")
                .password("pass")
                .role(UserRole.USER)
                .active(true)
                .build();

        Authentication auth =
                new UsernamePasswordAuthenticationToken(user.getEmail(), "pass", Collections.emptyList());

        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(userRepository.findByEmailAndActiveTrue("user@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("access-token");
        when(refreshTokenService.createRefreshToken(user))
                .thenReturn(new RefreshToken("refresh-token", user, Instant.now(), Instant.now().plusSeconds(10)));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                 { "email":"user@example.com",
                                   "password":"pass" }"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void login_invalidCredentials_401() throws Exception {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("bad credentials"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                 { "email":"user@example.com",
                                   "password":"wrong" }"""))
                .andExpect(status().isUnauthorized());
    }

    /* ---------- POST /auth/refresh ---------- */

    @Test
    void refresh_ok() throws Exception {
        User user = User.builder().id(1L).email("user@example.com").role(UserRole.USER).active(true).build();
        RefreshToken rt = new RefreshToken("refresh-token", user, Instant.now(), Instant.now().plusSeconds(10));

        when(refreshTokenService.findByToken("refresh-token")).thenReturn(Optional.of(rt));
        when(refreshTokenService.verifyExpiration(rt)).thenReturn(rt);
        when(jwtService.generateToken(user)).thenReturn("new-access");

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                 { "refreshToken":"refresh-token" }"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    void refresh_invalidToken_403() throws Exception {
        when(refreshTokenService.findByToken("bad")).thenReturn(Optional.empty());

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                 { "refreshToken":"bad" }"""))
                .andExpect(status().isForbidden());
    }

    /* ---------- DELETE /auth/logout ---------- */

    @Test
    void logout_ok() throws Exception {
        when(refreshTokenService.findByToken("rt")).thenReturn(Optional.of(
                new RefreshToken("rt", null, Instant.now(), Instant.now())));

        mockMvc.perform(delete("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                 { "refreshToken":"rt" }"""))
                .andExpect(status().isNoContent());
    }

    @Test
    void logout_invalidBody_204() throws Exception {
        when(refreshTokenService.findByToken(null)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNoContent());
    }
}
