package com.example.library;

import com.example.library.dto.ReservationDTO;
import com.example.library.exception.BadRequestException;
import com.example.library.exception.NotFoundException;
import com.example.library.model.*;
import com.example.library.repository.CopyRepository;
import com.example.library.repository.ReservationRepository;
import com.example.library.repository.UserRepository;
import com.example.library.service.NotificationService;
import com.example.library.service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReservationServiceTest {

    @InjectMocks
    private ReservationService reservationService;

    @Mock private ReservationRepository reservationRepository;
    @Mock private CopyRepository copyRepository;
    @Mock private UserRepository userRepository;
    @Mock private NotificationService notificationService;

    @Mock private Authentication authentication;

    private final Clock fixedClock = Clock.fixed(Instant.parse("2024-06-04T00:00:00Z"), ZoneId.systemDefault());

    private final User testUser = User.builder().id(1L).email("user@example.com").active(true).build();
    private final Library testLibrary = Library.builder().id(1L).name("Central Library").build();
    private final Book testBook = Book.builder().id(1L).title("Sample Book").build();
    private final Copy testCopy = Copy.builder().id(1L).status(CopyStatus.AVAILABLE).book(testBook).library(testLibrary).build();
    private final Reservation testReservation = Reservation.builder()
            .id(1L)
            .user(testUser)
            .copy(testCopy)
            .createdAt(LocalDate.now(fixedClock))
            .expirationDate(LocalDate.now(fixedClock).plusDays(2))
            .status(ReservationStatus.WAITING)
            .build();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        reservationService = new ReservationService(reservationRepository, copyRepository, userRepository, notificationService, fixedClock);

        SecurityContextHolder.getContext().setAuthentication(authentication);
        when(authentication.getName()).thenReturn(testUser.getEmail());
        when(userRepository.findByEmailAndActiveTrue(testUser.getEmail())).thenReturn(Optional.of(testUser));
    }

    @Test
    void testGetMyReservations_returnsUserReservations() {
        when(reservationRepository.findAllByUserId(1L)).thenReturn(List.of(testReservation));

        List<ReservationDTO> reservations = reservationService.getMyReservations();

        assertThat(reservations).hasSize(1);
        assertThat(reservations.get(0).email()).isEqualTo("user@example.com");
    }

    @Test
    void testGetMyActiveReservations_returnsOnlyWaitingStatus() {
        when(reservationRepository.findAllByUserIdAndStatus(1L, ReservationStatus.WAITING)).thenReturn(List.of(testReservation));

        List<ReservationDTO> reservations = reservationService.getMyActiveReservations();

        assertThat(reservations).hasSize(1);
        assertThat(reservations.get(0).status()).isEqualTo(ReservationStatus.WAITING);
    }

    @Test
    void testReserveCopy_successfullyReservesAvailableCopy() {
        testCopy.setStatus(CopyStatus.AVAILABLE);

        when(copyRepository.findById(1L)).thenReturn(Optional.of(testCopy));
        when(reservationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        reservationService.reserveCopy(1L);

        verify(copyRepository).save(argThat(copy -> copy.getStatus() == CopyStatus.RESERVED));
        verify(reservationRepository).save(any());
        verify(notificationService).sendAcceptedReservationNotification(eq("user@example.com"), any());
    }

    @Test
    void testReserveCopy_failsIfCopyUnavailable() {
        testCopy.setStatus(CopyStatus.BORROWED);
        when(copyRepository.findById(1L)).thenReturn(Optional.of(testCopy));

        assertThatThrownBy(() -> reservationService.reserveCopy(1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Copy is not available");
    }

    @Test
    void testCancelReservation_success() {
        testCopy.setStatus(CopyStatus.RESERVED);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(testReservation));

        reservationService.cancelReservation(1L);

        verify(reservationRepository).save(argThat(r -> r.getStatus() == ReservationStatus.CANCELLED));
        verify(copyRepository).save(argThat(c -> c.getStatus() == CopyStatus.AVAILABLE));
        verify(notificationService).sendCancelReservationNotification(eq("user@example.com"), any());
    }

    @Test
    void testCancelReservation_alreadyCancelled() {
        testReservation.setStatus(ReservationStatus.CANCELLED);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(testReservation));

        assertThatThrownBy(() -> reservationService.cancelReservation(1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already cancelled");
    }

    @Test
    void testGetReservationById_found() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(testReservation));

        ReservationDTO dto = reservationService.getReservationById(1L);

        assertThat(dto.userId()).isEqualTo(testUser.getId());
    }

    @Test
    void testGetReservationById_notFound() {
        when(reservationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.getReservationById(99L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void testGetReservationsByLibrary_returnsReservationsForLibrary() {
        when(reservationRepository.findByCopyLibraryId(1L)).thenReturn(List.of(testReservation));

        List<ReservationDTO> reservations = reservationService.getReservationsByLibrary(1L);

        assertThat(reservations).hasSize(1);
        assertThat(reservations.get(0).libraryName()).isEqualTo("Central Library");
    }

    @Test
    void testGetUserAllReservations_returnsReservationsForUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(reservationRepository.findAllByUserId(1L)).thenReturn(List.of(testReservation));

        List<ReservationDTO> reservations = reservationService.getUserAllReservations(1L);

        assertThat(reservations).hasSize(1);
        assertThat(reservations.get(0).email()).isEqualTo("user@example.com");
    }

    @Test
    void testGetUserAllReservations_throwsWhenLoggedInUserNotFound() {
        when(authentication.getName()).thenReturn("missing@example.com");
        when(userRepository.findByEmailAndActiveTrue("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.getUserAllReservations(2L))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found");
    }


    @Test
    void testCheckReservations_handlesExpiredAndReminders() {
        Reservation expired = Reservation.builder()
                .id(2L)
                .status(ReservationStatus.WAITING)
                .expirationDate(LocalDate.now(fixedClock).minusDays(1))
                .user(testUser)
                .copy(testCopy)
                .build();

        Reservation reminder = Reservation.builder()
                .id(3L)
                .status(ReservationStatus.WAITING)
                .expirationDate(LocalDate.now(fixedClock).plusDays(1))
                .user(testUser)
                .copy(testCopy)
                .build();

        when(reservationRepository.findAllByExpirationDateBeforeAndStatus(LocalDate.now(fixedClock), ReservationStatus.WAITING))
                .thenReturn(List.of(expired));
        when(reservationRepository.findByExpirationDate(LocalDate.now(fixedClock).plusDays(1)))
                .thenReturn(List.of(reminder));

        reservationService.checkReservations();

        verify(reservationRepository).save(expired);
        verify(copyRepository).save(expired.getCopy());
        verify(notificationService).sendCancelReservationNotification(eq("user@example.com"), eq(expired));
        verify(notificationService).sendOneDayLeftNotification(eq("user@example.com"), eq(reminder));
    }

}