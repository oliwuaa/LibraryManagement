package com.example.library.HttpIntegrationTest;

import com.example.library.component.JwtAuthenticationFilter;
import com.example.library.controller.UserController;
import com.example.library.dto.UserInfoDTO;
import com.example.library.dto.UserRegistrationDTO;
import com.example.library.exception.BadRequestException;
import com.example.library.exception.NotFoundException;
import com.example.library.model.*;
import com.example.library.service.AuthorizationService;
import com.example.library.service.JwtService;
import com.example.library.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(username = "admin", roles = "ADMIN")
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    UserService userService;

    @MockitoBean(name = "authorizationService")
    AuthorizationService authorizationService;

    @MockitoBean
    JwtService jwtService;

    @MockitoBean
    JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void allowAllSpEL() {
        when(authorizationService.isUserInLibrarianLibrary(anyLong())).thenReturn(true);
        when(authorizationService.isSelf(anyLong())).thenReturn(true);
        when(authorizationService.isLibrarianOfLibrary(anyLong())).thenReturn(true);
    }

    private UserInfoDTO dto(Long id) {
        return new UserInfoDTO(id, "mail"+id+"@x.com", "John", "Doe", "USER", null);
    }
    private User user(Long id, UserRole role) {
        return User.builder()
                .id(id)
                .email("john"+id+"@x.com")
                .name("John")
                .surname("Doe")
                .password("secret")
                .role(role)
                .build();
    }

    /* ---------- GET /users ---------- */

    @Test
    void getAllUsers_ok() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(dto(1L), dto(2L)));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2));
    }

    @Test
    void getAllUsers_noContent() throws Exception {
        when(userService.getAllUsers()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/users"))
                .andExpect(status().isNoContent());
    }

    /* ---------- GET /users/{id} ---------- */

    @Test
    void getUserById_ok() throws Exception {
        when(userService.getUserById(5L)).thenReturn(dto(5L));

        mockMvc.perform(get("/users/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void getUserById_notFound() throws Exception {
        when(userService.getUserById(6L))
                .thenThrow(new NotFoundException("User with ID 6 does not exist"));

        mockMvc.perform(get("/users/6"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User with ID 6 does not exist"));
    }

    /* ---------- GET /users/library/{id}/librarians ---------- */

    @Test
    void getLibrariansByLibrary_ok() throws Exception {
        when(userService.getLibrariansFromLibrary(3L)).thenReturn(List.of(dto(9L)));

        mockMvc.perform(get("/users/library/3/librarians"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(9));
    }

    @Test
    void getLibrariansByLibrary_noContent() throws Exception {
        when(userService.getLibrariansFromLibrary(3L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/users/library/3/librarians"))
                .andExpect(status().isNoContent());
    }

    @Test
    void getLibrariansByLibrary_notFound() throws Exception {
        when(userService.getLibrariansFromLibrary(7L))
                .thenThrow(new NotFoundException("Library with ID 7 does not exist"));

        mockMvc.perform(get("/users/library/7/librarians"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Library with ID 7 does not exist"));
    }

    /* ---------- POST /users/register ---------- */

    @Test
    void registerUser_ok() throws Exception {
        String body = """
            {"email":"a@x.com","password":"123","name":"A","surname":"B"}
            """;

        mockMvc.perform(post("/users/register")
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("User registered successfully"));
    }

    @Test
    void registerUser_emailTaken_badRequest() throws Exception {
        doThrow(new BadRequestException("Email already taken"))
                .when(userService).addUser(any(UserRegistrationDTO.class));

        mockMvc.perform(post("/users/register")
                        .content("""
                                {"email":"a@x.com","password":"123","name":"A","surname":"B"}
                                """)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Email already taken"));
    }

    /* ---------- PUT /users/{id} ---------- */

    @Test
    void updateUser_ok() throws Exception {
        String body = """
            {"id":1,"email":"x@y.com","name":"New","surname":"Name","role":"USER","libraryId":null}
            """;

        mockMvc.perform(put("/users/1")
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("User updated successfully"));
    }

    @Test
    void updateUser_emailTaken_badRequest() throws Exception {
        doThrow(new BadRequestException("Email already taken"))
                .when(userService).updateUser(eq(1L), any());

        mockMvc.perform(put("/users/1")
                        .content("""
                            {"id":1,"email":"dup@y.com","name":"A","surname":"B","role":"USER","libraryId":null}
                            """)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Email already taken"));
    }

    @Test
    void updateUser_notFound() throws Exception {
        doThrow(new NotFoundException("User with ID 9 does not exist"))
                .when(userService).updateUser(eq(9L), any());

        mockMvc.perform(put("/users/9")
                        .content("""
                            {"id":9,"email":"z@x.com","name":"Z","surname":"X","role":"USER","libraryId":null}
                            """)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User with ID 9 does not exist"));
    }

    /* ---------- PATCH /users/{id} (change role) ---------- */

    @Test
    void changeRole_ok() throws Exception {
        mockMvc.perform(patch("/users/2")
                        .content("\"ADMIN\"")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("User updated successfully"));
    }

    @Test
    void changeRole_duplicate_badRequest() throws Exception {
        doThrow(new BadRequestException("User already has this role."))
                .when(userService).changeRole(2L, UserRole.ADMIN, null);

        mockMvc.perform(patch("/users/2")
                        .content("\"ADMIN\"")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("User already has this role."));
    }

    @Test
    void changeRole_notFound() throws Exception {
        doThrow(new NotFoundException("User with ID 8 does not exist"))
                .when(userService).changeRole(8L, UserRole.ADMIN, null);

        mockMvc.perform(patch("/users/8")
                        .content("\"ADMIN\"")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User with ID 8 does not exist"));
    }

    /* ---------- DELETE /users/{id} ---------- */

    @Test
    void deleteUser_ok() throws Exception {
        mockMvc.perform(delete("/users/4"))
                .andExpect(status().isOk())
                .andExpect(content().string("User deleted successfully"));
    }

    @Test
    void deleteUser_notFound() throws Exception {
        doThrow(new NotFoundException("User with ID 11 does not exist"))
                .when(userService).deleteUser(11L);

        mockMvc.perform(delete("/users/11"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User with ID 11 does not exist"));
    }

    /* ---------- GET /users/search ---------- */

    @Test
    void searchUsers_ok() throws Exception {
        when(userService.searchUsers("John", null, null))
                .thenReturn(List.of(dto(13L)));

        mockMvc.perform(get("/users/search").param("name", "John"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(13));
    }

    @Test
    void searchUsers_noContent() throws Exception {
        when(userService.searchUsers(null, null, null))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/users/search"))
                .andExpect(status().isNoContent());
    }

    /* ---------- GET /users/me ---------- */

    @Test
    @WithMockUser(username = "user1", roles = "USER")
    void getCurrentUserInfo_ok() throws Exception {
        when(userService.getCurrentUser()).thenReturn(user(20L, UserRole.USER));

        mockMvc.perform(get("/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(20))
                .andExpect(jsonPath("$.role").value("USER"));
    }
}
