package com.example.library.repository;

import com.example.library.model.Copy;
import com.example.library.model.Reservation;
import com.example.library.model.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findAllByStatus(ReservationStatus status);
    List<Reservation> findAllByUserId(Long userId);
    List<Reservation> findAllByExpirationDateBeforeAndStatus(LocalDateTime date, ReservationStatus status);
    List<Reservation> findByCopyAndStatus(Copy copy,ReservationStatus status);
}
