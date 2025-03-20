package com.example.library.service;

import com.example.library.model.*;
import com.example.library.repository.CopyRepository;
import com.example.library.repository.ReservationRepository;
import com.example.library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final CopyRepository copyRepository;
    private final UserRepository userRepository;

    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    public List<Reservation> getUserAllReservations(Long userId) {
        return reservationRepository.findAllByUserId(userId);
    }

    public void reserveCopy(Long userId, Long copyId) {
        Copy copy = copyRepository.findById(copyId)
                .orElseThrow(() -> new RuntimeException("Copy not found!"));

        if (copy.getStatus() != CopyStatus.AVAILABLE) {
            throw new RuntimeException("Copy is not available for reservation!");
        }

        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalStateException("User with ID " + userId + " does not exist"));

        copy.setStatus(CopyStatus.RESERVED);
        copyRepository.save(copy);

        Reservation reservation = Reservation.builder().
                user(user).
                copy(copy).
                createdAt(LocalDateTime.now()).
                expirationDate(LocalDateTime.now().plusDays(2)).
                status(ReservationStatus.WAITING).
                build();

        reservationRepository.save(reservation);
    }

    public void cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(() -> new IllegalStateException("There's no such reservation"));
        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);
    }

    public void expiredReservations() {
        List<Reservation> expiredReservations = reservationRepository
                .findAllByExpirationDateBeforeAndStatus(LocalDateTime.now(), ReservationStatus.WAITING);

        for (Reservation reservation : expiredReservations) {
            reservation.setStatus(ReservationStatus.EXPIRED);
            reservationRepository.save(reservation);

            Copy copy = reservation.getCopy();
            copy.setStatus(CopyStatus.AVAILABLE);
            copyRepository.save(copy);
        }
    }


}
