package com.example.library.HttpIntegrationTest;

import com.example.library.component.JwtAuthenticationFilter;
import com.example.library.controller.CopyController;
import com.example.library.exception.BadRequestException;
import com.example.library.exception.NotFoundException;
import com.example.library.model.*;
import com.example.library.service.AuthorizationService;
import com.example.library.service.CopyService;
import com.example.library.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CopyController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(username = "admin", roles = "ADMIN")
class CopyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    CopyService copyService;

    @MockitoBean(name = "authorizationService")
    AuthorizationService authorizationService;

    @MockitoBean
    JwtService jwtService;

    @MockitoBean
    JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void allowAllSpELChecks() {
        when(authorizationService.isLibrarianOfLibrary(anyLong())).thenReturn(true);
        when(authorizationService.isCopyInLibrarianLibrary(anyLong())).thenReturn(true);
    }

    private Copy buildCopy(long id, CopyStatus status) {
        Book book = Book.builder().id(1L).title("Demo").author("None").isbn("123").build();
        Library lib = Library.builder().id(1L).name("Main").address("Addr").build();
        return Copy.builder().id(id).book(book).library(lib).status(status).build();
    }

    /* ---------- GET /copies ---------- */

    @Test
    void getAllCopies_ok() throws Exception {
        when(copyService.getAllCopies()).thenReturn(List.of(buildCopy(1L, CopyStatus.AVAILABLE)));

        mockMvc.perform(get("/copies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getAllCopies_noContent() throws Exception {
        when(copyService.getAllCopies()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/copies"))
                .andExpect(status().isNoContent());
    }

    /* ---------- GET /copies/{id} ---------- */

    @Test
    void getCopyById_ok() throws Exception {
        when(copyService.getCopyById(5L)).thenReturn(buildCopy(5L, CopyStatus.BORROWED));

        mockMvc.perform(get("/copies/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void getCopyById_notFound() throws Exception {
        when(copyService.getCopyById(5L))
                .thenThrow(new NotFoundException("Copy with ID 5 does not exist"));

        mockMvc.perform(get("/copies/5"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Copy with ID 5 does not exist"));
    }

    /* ---------- POST /copies/{bookId}/{libraryId} ---------- */

    @Test
    void addCopy_ok() throws Exception {
        mockMvc.perform(post("/copies/1/2"))
                .andExpect(status().isOk())
                .andExpect(content().string("Copy added successfully"));
    }

    @Test
    void addCopy_bookNotFound() throws Exception {
        doThrow(new NotFoundException("Book with ID 99 does not exist"))
                .when(copyService).addCopy(99L, 2L);

        mockMvc.perform(post("/copies/99/2"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Book with ID 99 does not exist"));
    }

    /* ---------- PUT /copies/{id} ---------- */

    @Test
    void changeStatus_ok() throws Exception {
        mockMvc.perform(put("/copies/3")
                        .content("\"BORROWED\"")
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(content().string("Copy status changed successfully"));
    }

    @Test
    void changeStatus_badRequest_duplicate() throws Exception {
        doThrow(new BadRequestException("Copy already has this status."))
                .when(copyService).updateCopyStatus(3L, CopyStatus.BORROWED);

        mockMvc.perform(put("/copies/3")
                        .content("\"BORROWED\"")
                        .contentType("application/json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Copy already has this status."));
    }

    @Test
    void changeStatus_notFound() throws Exception {
        doThrow(new NotFoundException("Copy with ID 42 does not exist"))
                .when(copyService).updateCopyStatus(42L, CopyStatus.AVAILABLE);

        mockMvc.perform(put("/copies/42")
                        .content("\"AVAILABLE\"")
                        .contentType("application/json"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Copy with ID 42 does not exist"));
    }

    /* ---------- DELETE /copies/{id} ---------- */

    @Test
    void deleteCopy_ok() throws Exception {
        mockMvc.perform(delete("/copies/4"))
                .andExpect(status().isOk())
                .andExpect(content().string("Copy deleted successfully"));
    }

    @Test
    void deleteCopy_badRequest() throws Exception {
        doThrow(new BadRequestException("Cannot delete copy – it is currently borrowed."))
                .when(copyService).deleteCopy(4L);

        mockMvc.perform(delete("/copies/4"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Cannot delete copy – it is currently borrowed."));
    }

    @Test
    void deleteCopy_notFound() throws Exception {
        doThrow(new NotFoundException("Copy with ID 44 does not exist"))
                .when(copyService).deleteCopy(44L);

        mockMvc.perform(delete("/copies/44"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Copy with ID 44 does not exist"));
    }
}
