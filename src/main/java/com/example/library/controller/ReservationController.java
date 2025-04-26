package com.example.library.controller;

import com.example.library.dto.ReservationDTO;
import com.example.library.model.Reservation;
import com.example.library.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Reservation made successfully",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(example = "Reservation made successfully")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request (e.g., copy not available)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"error\": \"Copy is not available for reservation!\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Copy not found or user not found in the system",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "Copy Not Found", value = "{\"error\": \"Copy not found!\"}"),
                                    @ExampleObject(name = "User Not Found", value = "{\"error\": \"User not found\"}")
                            }
                    )
            )
    })
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> makeBookReservation(
            @Parameter(description = "ID of the copy to reserve", example = "3")
            @RequestParam Long copyId
    ) {
        reservationService.reserveCopy(copyId);
        return ResponseEntity.ok("Reservation made successfully");
    }

    @Operation(
            summary = "Get all reservations.",
            description = "Returns a list of all reservations in the system. If no reservations exist, returns HTTP 204 No Content."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "List of reservations returned successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = Reservation.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "204",
                    description = "No reservations found",
                    content = @Content
            )
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<List<ReservationDTO>> getReservations() {
        List<ReservationDTO> reservations = reservationService.getAllReservations();
        if (reservations.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(reservations);
    }

    @Operation(
            summary = "Get all user's reservations.",
            description = "Returns a list of all reservations associated with a specific user. The user is identified by their ID."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "List of user's reservations returned successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = Reservation.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "204",
                    description = "No reservations found for this user",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"error\": \"User with ID 12 does not exist\"}")
                    )
            )
    })
    @PreAuthorize("hasRole('ADMIN') or " +
            "hasRole('Librarian') and @authorizationService.isUserInLibrarianLibrary(#userId) or " +
            "hasRole('USER') and @authorizationService.isSelf(#userId) ")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReservationDTO>> getUserReservations(
            @Parameter(description = "ID of the user whose reservations are to be fetched", example = "12")
            @PathVariable Long userId
    ) {
        List<ReservationDTO> reservations = reservationService.getUserAllReservations(userId);
        if (reservations.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(reservations);
    }

    @Operation(
            summary = "Get reservation by ID",
            description = "Returns a reservation with the specified ID."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Reservation found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Reservation.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Reservation not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"error\": \"Reservation with ID 99 does not exist\"}")
                    )
            )
    })
    @GetMapping("/{reservationId}")
    @PreAuthorize("hasRole('ADMIN') or " +
            "hasRole('LIBRARIAN') and @authorizationService.isReservationInLibrarianLibrary(#reservationId) or " +
            "hasRole('USER') and @authorizationService.isUserReservation(#reservationId) ")
    public ResponseEntity<ReservationDTO> getReservationById(
            @Parameter(description = "ID of the reservation to retrieve", example = "42")
            @PathVariable Long reservationId
    ) {
        return ResponseEntity.ok(reservationService.getReservationById(reservationId));
    }

    @Operation(
            summary = "Cancel a reservation.",
            description = "Cancels the reservation with the given ID and makes the associated copy available again."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Reservation canceled successfully",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(example = "Reservation canceled successfully")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Reservation already canceled",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"error\": \"Reservation with ID 42 is already cancelled\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Reservation not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"error\": \"Reservation with ID 99 does not exist\"}")
                    )
            )
    })
    @PatchMapping("/{reservationId}/cancel")
    @PreAuthorize("hasRole('ADMIN') or " +
            "hasRole('Librarian') and @authorizationService.isReservationInLibrarianLibrary(#reservationId) or " +
            "hasRole('USER') and @authorizationService.isUserReservation(#reservationId) ")
    public ResponseEntity<String> cancelReservation(
            @Parameter(description = "ID of the reservation to cancel", example = "5")
            @PathVariable Long reservationId
    ) {
        reservationService.cancelReservation(reservationId);
        return ResponseEntity.ok("Reservation canceled successfully");
    }
}