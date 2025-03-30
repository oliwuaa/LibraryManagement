package com.example.library.controller;

import com.example.library.model.Reservation;
import com.example.library.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("reservations")
public class ReservationController {

    private final ReservationService reservationService;

    @Operation(
            summary = "Reserve a copy.",
            description = "Reserves a copy with the given ID for a specific user. The reservation can be canceled, realized if the user borrows the copy, or expired if not collected on time."
    )
    @PostMapping
    public ResponseEntity<String> makeBookReservation(@RequestParam Long userId, @RequestParam Long copyId) {
        try {
            reservationService.reserveCopy(userId, copyId);
            return ResponseEntity.ok("Reservation made successfully");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An unexpected error occurred");
        }
    }

    @Operation(summary = "Get all reservations.", description = "Returns a list of all reservations in the system.")
    @GetMapping
    public ResponseEntity<List<Reservation>> getReservations() {
        List<Reservation> reservations = reservationService.getAllReservations();
        if (reservations.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(reservations);
    }


    @Operation(summary = "Get all user's reservations.",
            description = "Returns a list of all reservations associated with a specific user in the system. " +
                    "The reservations will be returned based on the user's ID")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Reservation>> getUserReservations(@PathVariable Long userId) {
        List<Reservation> reservations = reservationService.getUserAllReservations(userId);
        if (reservations.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(reservations);
    }

    @Operation(summary = "Cancel a reservation.",
            description = "Cancels a reservations with given Id.")
    @PatchMapping("/{reservationId}/cancel")
    public ResponseEntity<String> cancelReservation(@PathVariable Long reservationId) {
        try {
            reservationService.cancelReservation(reservationId);
            return ResponseEntity.ok("Reservation canceled successfully");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An unexpected error occurred");
        }
    }
}
