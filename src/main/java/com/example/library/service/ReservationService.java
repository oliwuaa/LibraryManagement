package com.example.library.service;

import com.example.library.dto.ReservationDTO;
import com.example.library.exception.BadRequestException;
import com.example.library.exception.NotFoundException;
import com.example.library.model.*;
import com.example.library.repository.CopyRepository;
import com.example.library.repository.ReservationRepository;
import com.example.library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final CopyRepository copyRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public List<ReservationDTO> getMyReservations() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User currentUser = userRepository.findByEmailAndActiveTrue(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return reservationRepository.findAllByUserId(currentUser.getId()).stream()
                .map(reservation -> new ReservationDTO(
                        reservation.getId(),
                        reservation.getUser().getId(),
                        reservation.getUser().getEmail(),
                        reservation.getCopy().getId(),
                        reservation.getCopy().getBook().getTitle(),
                        reservation.getCopy().getLibrary().getId(),
                        reservation.getCopy().getLibrary().getName(),
                        reservation.getCreatedAt(),
                        reservation.getExpirationDate(),
                        reservation.getStatus()
                ))
                .toList();
    }

    public List<ReservationDTO> getMyActiveReservations() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User currentUser = userRepository.findByEmailAndActiveTrue(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return reservationRepository.findAllByUserIdAndStatus(currentUser.getId(),ReservationStatus.WAITING ).stream()
                .map(reservation -> new ReservationDTO(
                        reservation.getId(),
                        reservation.getUser().getId(),
                        reservation.getUser().getEmail(),
                        reservation.getCopy().getId(),
                        reservation.getCopy().getBook().getTitle(),
                        reservation.getCopy().getLibrary().getId(),
                        reservation.getCopy().getLibrary().getName(),
                        reservation.getCreatedAt(),
                        reservation.getExpirationDate(),
                        reservation.getStatus()
                ))
                .toList();
    }



    public List<ReservationDTO> getAllReservations() {
        return reservationRepository.findAll().stream()
                .map(reservation -> new ReservationDTO(
                        reservation.getId(),
                        reservation.getUser().getId(),
                        reservation.getUser().getEmail(),
                        reservation.getCopy().getId(),
                        reservation.getCopy().getBook().getTitle(),
                        reservation.getCopy().getLibrary().getId(),
                        reservation.getCopy().getLibrary().getName(),
                        reservation.getCreatedAt(),
                        reservation.getExpirationDate(),
                        reservation.getStatus()
                ))
                .toList();
    }

    public List<ReservationDTO> getUserAllReservations(Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User currentUser = userRepository.findByEmailAndActiveTrue(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return reservationRepository.findAllByUserId(userId).stream()
                .map(reservation -> new ReservationDTO(
                        reservation.getId(),
                        reservation.getUser().getId(),
                        reservation.getUser().getEmail(),
                        reservation.getCopy().getId(),
                        reservation.getCopy().getBook().getTitle(),
                        reservation.getCopy().getLibrary().getId(),
                        reservation.getCopy().getLibrary().getName(),
                        reservation.getCreatedAt(),
                        reservation.getExpirationDate(),
                        reservation.getStatus()
                ))
                .toList();
    }

    public ReservationDTO getReservationById(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NotFoundException("Reservation with ID " + reservationId + " does not exist"));

        return new ReservationDTO(
                reservation.getId(),
                reservation.getUser().getId(),
                reservation.getUser().getEmail(),
                reservation.getCopy().getId(),
                reservation.getCopy().getBook().getTitle(),
                reservation.getCopy().getLibrary().getId(),
                reservation.getCopy().getLibrary().getName(),
                reservation.getCreatedAt(),
                reservation.getExpirationDate(),
                reservation.getStatus()
        );
    }

    public void reserveCopy(Long copyId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmailAndActiveTrue(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Copy copy = copyRepository.findById(copyId)
                .orElseThrow(() -> new NotFoundException("Copy not found!"));

        if (copy.getStatus() != CopyStatus.AVAILABLE) {
            throw new BadRequestException("Copy is not available for reservation!");
        }

        copy.setStatus(CopyStatus.RESERVED);
        copyRepository.save(copy);

        Reservation reservation = Reservation.builder()
                .user(user)
                .copy(copy)
                .createdAt(LocalDate.now())
                .expirationDate(LocalDate.now().plusDays(2))
                .status(ReservationStatus.WAITING)
                .build();

        reservationRepository.save(reservation);
        notificationService.sendAcceptedReservationNotification(reservation.getUser().getEmail(), reservation);
    }

    public void cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NotFoundException("Reservation with ID " + reservationId + " does not exist"));

        if (reservation.getStatus() != ReservationStatus.WAITING) {
            throw new BadRequestException("Reservation with ID " + reservationId + " is already cancelled");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);

        Copy copy = reservation.getCopy();
        copy.setStatus(CopyStatus.AVAILABLE);
        copyRepository.save(copy);

        notificationService.sendCancelReservationNotification(reservation.getUser().getEmail(), reservation);
    }

    @Scheduled(cron = "0 */5 * * * *")
    public void checkReservations() {
        List<Reservation> expiredReservations = reservationRepository.findAllByExpirationDateBeforeAndStatus(LocalDate.now(), ReservationStatus.WAITING);

        for (Reservation reservation : expiredReservations) {
            reservation.setStatus(ReservationStatus.EXPIRED);
            reservationRepository.save(reservation);
            notificationService.sendCancelReservationNotification(reservation.getUser().getEmail(), reservation);

            Copy copy = reservation.getCopy();
            copy.setStatus(CopyStatus.AVAILABLE);
            copyRepository.save(copy);
        }

        List<Reservation> reservationsWithOneDayLeft = reservationRepository.findByExpirationDate(LocalDate.now().plusDays(1));
        for (Reservation reservation : reservationsWithOneDayLeft) {
            notificationService.sendOneDayLeftNotification(reservation.getUser().getEmail(), reservation);
        }
    }

    public List<ReservationDTO> getReservationsByLibrary(Long libraryId) {
        List<Reservation> reservations = reservationRepository.findByCopyLibraryId(libraryId);
        return reservations.stream()
                .map(reservation -> new ReservationDTO(
                        reservation.getId(),
                        reservation.getUser().getId(),
                        reservation.getUser().getEmail(),
                        reservation.getCopy().getId(),
                        reservation.getCopy().getBook().getTitle(),
                        reservation.getCopy().getLibrary().getId(),
                        reservation.getCopy().getLibrary().getName(),
                        reservation.getCreatedAt(),
                        reservation.getExpirationDate(),
                        reservation.getStatus()
                ))
                .toList();
    }
}