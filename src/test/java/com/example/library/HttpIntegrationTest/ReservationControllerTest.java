package com.example.library.HttpIntegrationTest;

import com.example.library.component.JwtAuthenticationFilter;
import com.example.library.controller.ReservationController;
import com.example.library.dto.ReservationDTO;
import com.example.library.exception.BadRequestException;
import com.example.library.exception.NotFoundException;
import com.example.library.model.ReservationStatus;
import com.example.library.service.AuthorizationService;
import com.example.library.service.JwtService;
import com.example.library.service.ReservationService;
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

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReservationController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(username = "admin", roles = "ADMIN")
class ReservationControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ReservationService reservationService;

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
        when(authorizationService.isReservationInLibrarianLibrary(anyLong())).thenReturn(true);
        when(authorizationService.isUserReservation(anyLong())).thenReturn(true);
        when(authorizationService.isLibrarianOfLibrary(anyLong())).thenReturn(true);
    }

    private ReservationDTO dto(long id, ReservationStatus status) {
        return new ReservationDTO(
                id,
                5L, "user@example.com",
                12L, "Demo Title",
                1L, "Main Library",
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 1, 15),
                status
        );
    }

    /* ---------- POST /reservations (make) ---------- */

    @Test
    void makeReservation_ok() throws Exception {
        mockMvc.perform(post("/reservations")
                        .param("copyId", "12")
                        .with(user("user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(content().string("Reservation made successfully"));
    }

    @Test
    void makeReservation_copyNotAvailable_badRequest() throws Exception {
        doThrow(new BadRequestException("Copy is not available for reservation!"))
                .when(reservationService).reserveCopy(12L);

        mockMvc.perform(post("/reservations")
                        .param("copyId", "12")
                        .with(user("user").roles("USER")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Copy is not available for reservation!"));
    }

    @Test
    void makeReservation_copyNotFound() throws Exception {
        doThrow(new NotFoundException("Copy not found!"))
                .when(reservationService).reserveCopy(99L);

        mockMvc.perform(post("/reservations")
                        .param("copyId", "99")
                        .with(user("user").roles("USER")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Copy not found!"));
    }

    /* ---------- GET /reservations ---------- */

    @Test
    void getAllReservations_ok() throws Exception {
        when(reservationService.getAllReservations()).thenReturn(List.of(dto(1, ReservationStatus.WAITING)));

        mockMvc.perform(get("/reservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("WAITING"));
    }

    @Test
    void getAllReservations_noContent() throws Exception {
        when(reservationService.getAllReservations()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/reservations"))
                .andExpect(status().isNoContent());
    }

    /* ---------- GET /reservations/user/{id} ---------- */

    @Test
    void getUserReservations_ok() throws Exception {
        when(reservationService.getUserAllReservations(5L)).thenReturn(List.of(dto(10, ReservationStatus.WAITING)));

        mockMvc.perform(get("/reservations/user/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10));
    }

    @Test
    void getUserReservations_noContent() throws Exception {
        when(reservationService.getUserAllReservations(5L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/reservations/user/5"))
                .andExpect(status().isNoContent());
    }

    @Test
    void getUserReservations_userNotFound() throws Exception {
        when(reservationService.getUserAllReservations(12L))
                .thenThrow(new NotFoundException("User with ID 12 does not exist"));

        mockMvc.perform(get("/reservations/user/12"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User with ID 12 does not exist"));
    }

    /* ---------- GET /reservations/{id} ---------- */

    @Test
    void getReservationById_ok() throws Exception {
        when(reservationService.getReservationById(42L)).thenReturn(dto(42, ReservationStatus.WAITING));

        mockMvc.perform(get("/reservations/42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.libraryName").value("Main Library"));
    }

    @Test
    void getReservationById_notFound() throws Exception {
        when(reservationService.getReservationById(99L))
                .thenThrow(new NotFoundException("Reservation with ID 99 does not exist"));

        mockMvc.perform(get("/reservations/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Reservation with ID 99 does not exist"));
    }

    /* ---------- PUT /reservations/{id}/cancel ---------- */

    @Test
    void cancelReservation_ok() throws Exception {
        mockMvc.perform(put("/reservations/5/cancel"))
                .andExpect(status().isOk())
                .andExpect(content().string("Reservation canceled successfully"));
    }

    @Test
    void cancelReservation_alreadyCancelled_badRequest() throws Exception {
        doThrow(new BadRequestException("Reservation with ID 5 is already cancelled"))
                .when(reservationService).cancelReservation(5L);

        mockMvc.perform(put("/reservations/5/cancel"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Reservation with ID 5 is already cancelled"));
    }

    @Test
    void cancelReservation_notFound() throws Exception {
        doThrow(new NotFoundException("Reservation with ID 77 does not exist"))
                .when(reservationService).cancelReservation(77L);

        mockMvc.perform(put("/reservations/77/cancel"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Reservation with ID 77 does not exist"));
    }

    /* ---------- GET /reservations/me (+ /active) ---------- */

    @Test
    void getMyReservations_ok() throws Exception {
        when(reservationService.getMyReservations()).thenReturn(List.of(dto(3, ReservationStatus.WAITING)));

        mockMvc.perform(get("/reservations/me")
                        .with(user("user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(3));
    }

    @Test
    void getMyActiveReservations_ok() throws Exception {
        when(reservationService.getMyActiveReservations()).thenReturn(List.of(dto(4, ReservationStatus.WAITING)));

        mockMvc.perform(get("/reservations/me/active")
                        .with(user("user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(4));
    }

    /* ---------- GET /reservations/library/{id} ---------- */

    @Test
    void getReservationsByLibrary_ok() throws Exception {
        when(reservationService.getReservationsByLibrary(1L)).thenReturn(List.of(dto(6, ReservationStatus.WAITING)));

        mockMvc.perform(get("/reservations/library/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].libraryId").value(1));
    }

    @Test
    void getReservationsByLibrary_noContent() throws Exception {
        when(reservationService.getReservationsByLibrary(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/reservations/library/1"))
                .andExpect(status().isNoContent());
    }
}
