package com.example.library;


import com.example.library.dto.LoanDTO;
import com.example.library.exception.BadRequestException;
import com.example.library.model.*;
import com.example.library.repository.CopyRepository;
import com.example.library.repository.LoanRepository;
import com.example.library.repository.ReservationRepository;
import com.example.library.repository.UserRepository;
import com.example.library.service.LoanService;
import com.example.library.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CopyRepository copyRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private NotificationService notificationService;

    private final Clock fixedClock = Clock.fixed(
            LocalDate.of(2025, 6, 1).atStartOfDay(ZoneId.systemDefault()).toInstant(),
            ZoneId.systemDefault()
    );

    @InjectMocks
    private LoanService loanService;

    @BeforeEach
    void setUp() {
        loanService = new LoanService(
                loanRepository,
                userRepository,
                copyRepository,
                reservationRepository,
                notificationService,
                fixedClock
        );
    }


    @Test
    void shouldBorrowBookWhenCopyReserved() {
        Long userId = 1L;
        Long copyId = 2L;
        LocalDate now = LocalDate.now(fixedClock);
        User user = new User(); user.setId(userId); user.setEmail("user@example.com");
        Copy copy = new Copy(); copy.setId(copyId); copy.setStatus(CopyStatus.RESERVED);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(copyRepository.findById(copyId)).thenReturn(Optional.of(copy));
        when(reservationRepository.existsReservationByCopy_IdAndAndUser_IdAndStatus(copyId, userId, ReservationStatus.WAITING)).thenReturn(true);

        loanService.borrowBook(userId, copyId);
        ArgumentCaptor<Loan> captor = ArgumentCaptor.forClass(Loan.class);
        verify(loanRepository).save(captor.capture());
        Loan savedLoan = captor.getValue();


        verify(loanRepository).save(any(Loan.class));

        assertEquals(now, savedLoan.getStartDate());
        assertEquals(now.plusWeeks(2), savedLoan.getEndDate());
        assertEquals(user, savedLoan.getUser());
        assertEquals(copy, savedLoan.getCopy());
        assertNull(savedLoan.getReturnDate());
        assertEquals(CopyStatus.BORROWED, copy.getStatus());

        verify(copyRepository).save(copy);
        verify(notificationService).sendLoanSuccess(eq("user@example.com"), any(Loan.class));
    }

    @Test
    void shouldBorrowBookWhenCopyUnavailable() {
        Long userId = 1L;
        Long copyId = 2L;
        LocalDate now = LocalDate.now(fixedClock);
        User user = new User(); user.setId(userId); user.setEmail("user@example.com");
        Copy copy = new Copy(); copy.setId(copyId); copy.setStatus(CopyStatus.BORROWED);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(copyRepository.findById(copyId)).thenReturn(Optional.of(copy));
        when(reservationRepository.existsReservationByCopy_IdAndAndUser_IdAndStatus(copyId, userId, ReservationStatus.WAITING)).thenReturn(false);

        assertThrows(BadRequestException.class, () ->  loanService.borrowBook(userId, copyId));
        verifyNoInteractions(loanRepository);
    }

    @Test
    void shouldBorrowBookWhenCopyAvailable() {
        Long userId = 1L;
        Long copyId = 2L;
        LocalDate now = LocalDate.now(fixedClock);
        User user = new User(); user.setId(userId); user.setEmail("user@example.com");
        Copy copy = new Copy(); copy.setId(copyId); copy.setStatus(CopyStatus.AVAILABLE);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(copyRepository.findById(copyId)).thenReturn(Optional.of(copy));
        when(reservationRepository.existsReservationByCopy_IdAndAndUser_IdAndStatus(copyId, userId, ReservationStatus.WAITING)).thenReturn(false);

        loanService.borrowBook(userId, copyId);
        ArgumentCaptor<Loan> captor = ArgumentCaptor.forClass(Loan.class);
        verify(loanRepository).save(captor.capture());
        Loan savedLoan = captor.getValue();


        verify(loanRepository).save(any(Loan.class));

        assertEquals(now, savedLoan.getStartDate());
        assertEquals(now.plusWeeks(2), savedLoan.getEndDate());
        assertEquals(user, savedLoan.getUser());
        assertEquals(copy, savedLoan.getCopy());
        assertNull(savedLoan.getReturnDate());
        assertEquals(CopyStatus.BORROWED, copy.getStatus());

        verify(copyRepository).save(copy);
        verify(notificationService).sendLoanSuccess(eq("user@example.com"), any(Loan.class));
    }

    @Test
    void shouldReturnBookWhenLoanExistsAndNotReturned() {
        Loan loan = new Loan();
        loan.setId(1L);
        loan.setCopy(new Copy());
        loan.setReturnDate(null);

        when(loanRepository.findByIdAndReturnDateIsNull(1L)).thenReturn(Optional.of(loan));

        loanService.returnBook(1L);

        assertEquals(LocalDate.now(fixedClock), loan.getReturnDate());
        assertEquals(CopyStatus.AVAILABLE, loan.getCopy().getStatus());
    }

    @Test
    void shouldExtendLoanWhenNewDateAfterCurrent() {
        Loan loan = new Loan();
        loan.setId(1L);
        loan.setEndDate(LocalDate.of(2025, 6, 10));
        loan.setReturnDate(null);

        when(loanRepository.findByIdAndReturnDateIsNull(1L)).thenReturn(Optional.of(loan));

        loanService.extendLoan(LocalDate.of(2025, 6, 15), 1L);

        assertEquals(LocalDate.of(2025, 6, 15), loan.getEndDate());
        verify(loanRepository).save(loan);
    }


    @Test
    void shouldThrowWhenNewEndDateBeforeCurrent() {
        Loan loan = new Loan(); loan.setEndDate(LocalDate.of(2025, 6, 10));

        when(loanRepository.findByIdAndReturnDateIsNull(1L)).thenReturn(Optional.of(loan));

        assertThrows(BadRequestException.class, () -> loanService.extendLoan(LocalDate.of(2025, 6, 5), 1L));
    }

    @Test
    void shouldNotifyOverdueLoans() {
        Loan loan = new Loan(); loan.setUser(new User());
        loan.getUser().setEmail("user@example.com");

        when(loanRepository.findByEndDateBeforeAndReturnDateIsNull(LocalDate.now(fixedClock)))
                .thenReturn(List.of(loan));

        loanService.checkLoans();

        verify(notificationService).sendOverdueNotification(eq("user@example.com"), any(Loan.class));
    }

    @Test
    void shouldReturnMyLoans_whenUserIsAuthenticated() {
        User user = new User();
        user.setId(1L);
        user.setEmail("me@example.com");

        Book book = new Book();
        book.setTitle("Some Book");

        Library library = new Library();
        library.setId(10L);
        library.setName("Main Library");

        Copy copy = new Copy();
        copy.setId(2L);
        copy.setBook(book);
        copy.setLibrary(library);

        Loan loan = new Loan();
        loan.setId(3L);
        loan.setUser(user);
        loan.setCopy(copy);
        loan.setStartDate(LocalDate.of(2025, 6, 1));
        loan.setEndDate(LocalDate.of(2025, 6, 15));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("me@example.com", null, List.of())
        );

        when(userRepository.findByEmailAndActiveTrue("me@example.com")).thenReturn(Optional.of(user));
        when(loanRepository.findByUserId(1L)).thenReturn(List.of(loan));

        List<LoanDTO> result = loanService.getMyLoans();

        assertEquals(1, result.size());
        assertEquals("Some Book", result.get(0).title());

        SecurityContextHolder.clearContext();
    }

}