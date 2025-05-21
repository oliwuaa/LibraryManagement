package com.example.library.repository;

import com.example.library.model.Copy;
import com.example.library.model.Reservation;
import com.example.library.model.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findAllByUserIdAndStatus(Long userId, ReservationStatus status);
    List<Reservation> findAllByUserId(Long userId);
    List<Reservation> findAllByExpirationDateBeforeAndStatus(LocalDateTime date, ReservationStatus status);
    Optional<Reservation> findByCopy_IdAndStatus(Long copyId, ReservationStatus status);
    boolean existsReservationByCopy_IdAndAndUser_IdAndStatus(Long copyId, Long userID, ReservationStatus status);
    boolean existsReservationByCopy_Id(Long copyId);
    List<Reservation> findByExpirationDate(LocalDate date);
    Optional<Reservation> findReservationByCopy_Id(Long copyId);
    List<Reservation> findByCopyLibraryId(Long libraryId);
}
