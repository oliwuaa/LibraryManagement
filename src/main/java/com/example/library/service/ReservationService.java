package com.example.library.service;

import com.example.library.exception.BadRequestException;
import com.example.library.exception.NotFoundException;
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
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with ID " + userId + " does not exist");
        }

        return reservationRepository.findAllByUserId(userId);
    }

    public Reservation getReservationById(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NotFoundException("Reservation with ID " + reservationId + " does not exist"));
    }

    public void reserveCopy(Long userId, Long copyId) {
        Copy copy = copyRepository.findById(copyId)
                .orElseThrow(() -> new NotFoundException("Copy not found!"));

        if (copy.getStatus() != CopyStatus.AVAILABLE) {
            throw new BadRequestException("Copy is not available for reservation!");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with ID " + userId + " does not exist"));

        copy.setStatus(CopyStatus.RESERVED);
        copyRepository.save(copy);

        Reservation reservation = Reservation.builder()
                .user(user)
                .copy(copy)
                .createdAt(LocalDateTime.now())
                .expirationDate(LocalDateTime.now().plusDays(2))
                .status(ReservationStatus.WAITING)
                .build();

        reservationRepository.save(reservation);
    }

    public void cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NotFoundException("Reservation with ID " + reservationId + " does not exist"));

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new BadRequestException("Reservation with ID " + reservationId + " is already cancelled");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);

        Copy copy = reservation.getCopy();
        copy.setStatus(CopyStatus.AVAILABLE);
        copyRepository.save(copy);
    }


    public void expiredReservations() {
        List<Reservation> expiredReservations = reservationRepository.findAllByExpirationDateBeforeAndStatus(LocalDateTime.now(), ReservationStatus.WAITING);

        for (Reservation reservation : expiredReservations) {
            reservation.setStatus(ReservationStatus.EXPIRED);
            reservationRepository.save(reservation);

            Copy copy = reservation.getCopy();
            copy.setStatus(CopyStatus.AVAILABLE);
            copyRepository.save(copy);
        }
    }


}
