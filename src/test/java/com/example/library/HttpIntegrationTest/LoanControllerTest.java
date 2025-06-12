package com.example.library.HttpIntegrationTest;

import com.example.library.component.JwtAuthenticationFilter;
import com.example.library.controller.LoanController;
import com.example.library.dto.LoanDTO;
import com.example.library.exception.BadRequestException;
import com.example.library.exception.NotFoundException;
import com.example.library.service.AuthorizationService;
import com.example.library.service.JwtService;
import com.example.library.service.LoanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LoanController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(username = "admin", roles = "ADMIN")
class LoanControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    LoanService loanService;

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
        when(authorizationService.isCopyInLibrarianLibrary(anyLong())).thenReturn(true);
        when(authorizationService.isLoanInLibrarianLibrary(anyLong())).thenReturn(true);
        when(authorizationService.isLibrarianOfLibrary(anyLong())).thenReturn(true);
        when(authorizationService.isUserLoan(anyLong())).thenReturn(true);
    }

    private LoanDTO dto(long id) {
        return new LoanDTO(
                id,
                5L,
                "user@example.com",
                12L,
                "Demo Title",
                1L,
                "Main Library",
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 1, 15),
                null
        );
    }

    /* ---------- GET /loans ---------- */

    @Test
    void getAllLoans_ok() throws Exception {
        when(loanService.getAllLoans()).thenReturn(List.of(dto(1)));

        mockMvc.perform(get("/loans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Demo Title"));
    }

    @Test
    void getAllLoans_noContent() throws Exception {
        when(loanService.getAllLoans()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/loans"))
                .andExpect(status().isNoContent());
    }

    /* ---------- GET /loans/user/{id} ---------- */

    @Test
    void getUserLoans_ok() throws Exception {
        when(loanService.getAllUserLoan(5L)).thenReturn(List.of(dto(10)));

        mockMvc.perform(get("/loans/user/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].email").value("user@example.com"));
    }

    @Test
    void getUserLoans_noContent() throws Exception {
        when(loanService.getAllUserLoan(5L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/loans/user/5"))
                .andExpect(status().isNoContent());
    }

    @Test
    void getUserLoans_userNotFound() throws Exception {
        when(loanService.getAllUserLoan(42L))
                .thenThrow(new NotFoundException("User with ID 42 does not exist"));

        mockMvc.perform(get("/loans/user/42"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User with ID 42 does not exist"));
    }

    /* ---------- GET /loans/{id} ---------- */

    @Test
    void getLoanById_ok() throws Exception {
        when(loanService.getLoanById(10L)).thenReturn(dto(10));

        mockMvc.perform(get("/loans/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.libraryName").value("Main Library"));
    }

    @Test
    void getLoanById_notFound() throws Exception {
        when(loanService.getLoanById(10L))
                .thenThrow(new NotFoundException("Loan with ID 10 does not exist"));

        mockMvc.perform(get("/loans/10"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Loan with ID 10 does not exist"));
    }

    /* ---------- POST /loans (create) ---------- */

    @Test
    void createLoan_ok() throws Exception {
        mockMvc.perform(post("/loans")
                        .param("userId", "5")
                        .param("copyId", "12"))
                .andExpect(status().isOk())
                .andExpect(content().string("Book borrowed successfully"));
    }

    @Test
    void createLoan_copyNotAvailable_badRequest() throws Exception {
        doThrow(new BadRequestException("This copy isn't available"))
                .when(loanService).borrowBook(5L, 12L);

        mockMvc.perform(post("/loans")
                        .param("userId", "5")
                        .param("copyId", "12"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("This copy isn't available"));
    }

    @Test
    void createLoan_copyNotFound() throws Exception {
        doThrow(new NotFoundException("Copy with ID 33 does not exist"))
                .when(loanService).borrowBook(5L, 33L);

        mockMvc.perform(post("/loans")
                        .param("userId", "5")
                        .param("copyId", "33"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Copy with ID 33 does not exist"));
    }

    /* ---------- POST /loans/{id}/return ---------- */

    @Test
    void returnBook_ok() throws Exception {
        mockMvc.perform(post("/loans/1/return"))
                .andExpect(status().isOk())
                .andExpect(content().string("Book returned successfully"));
    }

    @Test
    void returnBook_badRequest() throws Exception {
        doThrow(new BadRequestException("This book has already been returned or loan doesn't exist"))
                .when(loanService).returnBook(1L);

        mockMvc.perform(post("/loans/1/return"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(
                        "This book has already been returned or loan doesn't exist"));
    }

    /* ---------- POST /loans/{id}/extend ---------- */

    @Test
    void extendLoan_ok() throws Exception {
        mockMvc.perform(post("/loans/10/extend")
                        .param("returnDate", "2030-01-30"))
                .andExpect(status().isOk())
                .andExpect(content().string("Loan extended successfully"));
    }

    @Test
    void extendLoan_badRequest_invalidDate() throws Exception {
        doThrow(new BadRequestException("The new date must be after endDate"))
                .when(loanService).extendLoan(LocalDate.of(2024, 1, 1), 10L);

        mockMvc.perform(post("/loans/10/extend")
                        .param("returnDate", "2024-01-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("The new date must be after endDate"));
    }

    @Test
    void extendLoan_notFound() throws Exception {
        doThrow(new NotFoundException("This loan doesn't exist"))
                .when(loanService).extendLoan(LocalDate.of(2030, 1, 30), 10L);

        mockMvc.perform(post("/loans/10/extend")
                        .param("returnDate", "2030-01-30"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("This loan doesn't exist"));
    }

    /* ---------- GET /loans/me ---------- */

    @Test
    void getMyLoans_ok() throws Exception {
        when(loanService.getMyLoans()).thenReturn(List.of(dto(7)));

        mockMvc.perform(get("/loans/me")
                        .with(user("user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(7));
    }

    /* ---------- GET /loans/me/active ---------- */

    @Test
    void getMyActiveLoans_ok() throws Exception {
        when(loanService.getMyActiveLoans()).thenReturn(List.of(dto(8)));

        mockMvc.perform(get("/loans/me/active")
                        .with(user("user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(8));
    }

    /* ---------- GET /loans/library/{id} ---------- */

    @Test
    void getLoansByLibrary_ok() throws Exception {
        when(loanService.getLoansByLibrary(1L)).thenReturn(List.of(dto(3)));

        mockMvc.perform(get("/loans/library/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].libraryName").value("Main Library"));
    }

    @Test
    void getLoansByLibrary_noContent() throws Exception {
        when(loanService.getLoansByLibrary(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/loans/library/1"))
                .andExpect(status().isNoContent());
    }
}
