package com.example.library.service;

import com.example.library.dto.LoanDTO;
import com.example.library.exception.BadRequestException;
import com.example.library.exception.NotFoundException;
import com.example.library.model.*;
import com.example.library.repository.CopyRepository;
import com.example.library.repository.LoanRepository;
import com.example.library.repository.ReservationRepository;
import com.example.library.repository.UserRepository;
import jakarta.transaction.Transactional;
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
public class LoanService {

    private final LoanRepository loanRepository;
    private final UserRepository userRepository;
    private final CopyRepository copyRepository;
    private final ReservationRepository reservationRepository;
    private final NotificationService notificationService;


    public List<LoanDTO> getAllLoans() {
        return loanRepository.findAll().stream()
                .map(loan -> new LoanDTO(
                        loan.getId(),
                        loan.getUser().getId(),
                        loan.getUser().getEmail(),
                        loan.getCopy().getId(),
                        loan.getCopy().getBook().getTitle(),
                        loan.getCopy().getLibrary().getId(),
                        loan.getStartDate(),
                        loan.getEndDate(),
                        loan.getReturnDate()
                ))
                .toList();
    }

    public List<LoanDTO> getMyLoans() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User currentUser = userRepository.findByEmailAndActiveTrue(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return loanRepository.findByUserId(currentUser.getId()).stream()
                .map(loan -> new LoanDTO(
                        loan.getId(),
                        loan.getUser().getId(),
                        loan.getUser().getEmail(),
                        loan.getCopy().getId(),
                        loan.getCopy().getBook().getTitle(),
                        loan.getCopy().getLibrary().getId(),
                        loan.getStartDate(),
                        loan.getEndDate(),
                        loan.getReturnDate()
                ))
                .toList();
    }

    public List<LoanDTO> getMyActiveLoans() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User currentUser = userRepository.findByEmailAndActiveTrue(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return loanRepository.findByUserIdAndReturnDateIsNull(currentUser.getId()).stream()
                .map(loan -> new LoanDTO(
                        loan.getId(),
                        loan.getUser().getId(),
                        loan.getUser().getEmail(),
                        loan.getCopy().getId(),
                        loan.getCopy().getBook().getTitle(),
                        loan.getCopy().getLibrary().getId(),
                        loan.getStartDate(),
                        loan.getEndDate(),
                        loan.getReturnDate()
                ))
                .toList();
    }

    public List<LoanDTO> getAllUserLoan(Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User currentUser = userRepository.findByEmailAndActiveTrue(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return loanRepository.findByUserId(userId).stream()
                .map(loan -> new LoanDTO(
                        loan.getId(),
                        loan.getUser().getId(),
                        loan.getUser().getEmail(),
                        loan.getCopy().getId(),
                        loan.getCopy().getBook().getTitle(),
                        loan.getCopy().getLibrary().getId(),
                        loan.getStartDate(),
                        loan.getEndDate(),
                        loan.getReturnDate()
                ))
                .toList();
    }

    public LoanDTO getLoanById(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new NotFoundException("Loan with ID " + loanId + " does not exist"));

        return new LoanDTO(
                loan.getId(),
                loan.getUser().getId(),
                loan.getUser().getEmail(),
                loan.getCopy().getId(),
                loan.getCopy().getBook().getTitle(),
                loan.getCopy().getLibrary().getId(),
                loan.getStartDate(),
                loan.getEndDate(),
                loan.getReturnDate()
        );
    }

    public List<LoanDTO> getLoansByLibrary(Long libraryId) {
        List<Loan> loans = loanRepository.findByCopy_Library_Id(libraryId);
        return loans.stream()
                .map(loan -> new LoanDTO(
                        loan.getId(),
                        loan.getUser().getId(),
                        loan.getUser().getEmail(),
                        loan.getCopy().getId(),
                        loan.getCopy().getBook().getTitle(),
                        loan.getCopy().getLibrary().getId(),
                        loan.getStartDate(),
                        loan.getEndDate(),
                        loan.getReturnDate()
                ))
                .toList();
    }


    public void borrowBook(Long userId, Long copyId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with ID " + userId + " does not exist"));

        Copy copy = copyRepository.findById(copyId)
                .orElseThrow(() -> new NotFoundException("Copy with ID " + copyId + " does not exist"));

        boolean hasReservation = reservationRepository.existsReservationByCopy_IdAndAndUser_IdAndStatus(copyId, userId, ReservationStatus.WAITING);

        if (copy.getStatus() != CopyStatus.AVAILABLE && !hasReservation) {
            throw new BadRequestException("This copy isn't available");
        }

        copy.setStatus(CopyStatus.BORROWED);
        copyRepository.save(copy);

        Loan loan = Loan.builder()
                .user(user)
                .copy(copy)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusWeeks(2))
                .build();
        loanRepository.save(loan);

        reservationRepository.findByCopy_IdAndStatus(copyId, ReservationStatus.WAITING)
                .ifPresent(reservation -> {
                    reservation.setStatus(ReservationStatus.REALIZED);
                    reservationRepository.save(reservation);
                });

        notificationService.sendLoanSuccess(loan.getUser().getEmail(), loan);
    }

    @Transactional
    public void returnBook(Long loanId) {
        Loan loan = loanRepository.findByIdAndReturnDateIsNull(loanId)
                .orElseThrow(() -> new BadRequestException("This book has already been returned or loan doesn't exist"));

        loan.setReturnDate(LocalDate.now());
        loan.getCopy().setStatus(CopyStatus.AVAILABLE);
    }

    public void extendLoan(LocalDate date, Long loanId) {
        Loan loan = loanRepository.findByIdAndReturnDateIsNull(loanId)
                .orElseThrow(() -> new NotFoundException("This loan doesn't exist"));

        if (date.isBefore(loan.getEndDate())) {
            throw new BadRequestException("The new date must be after endDate");
        }

        loan.setEndDate(date);
        loanRepository.save(loan);
    }

    @Scheduled(cron = "0 */5 * * * *")
    public void checkLoans() {
        List<Loan> overdueLoans = loanRepository.findByEndDateBeforeAndReturnDateIsNull(LocalDate.now());
        for (Loan loan : overdueLoans) {
            notificationService.sendOverdueNotification(loan.getUser().getEmail(), loan);
        }
    }
}