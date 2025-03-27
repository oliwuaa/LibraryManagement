package com.example.library.service;

import com.example.library.model.*;
import com.example.library.repository.CopyRepository;
import com.example.library.repository.LoanRepository;
import com.example.library.repository.ReservationRepository;
import com.example.library.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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


    public List<Loan> getAllLoans() {
        return loanRepository.findAll();
    }

    public List<Loan> getAllUserLoan(Long userId) {
        return loanRepository.findByUserId(userId);
    }

    public List<Loan> getUserActiveLoans(Long userId) {
        return loanRepository.findByUserIdAndReturnDateIsNull(userId);
    }


    public void borrowBook(Long userId, Long copyId) throws IllegalAccessException {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalStateException("User with ID " + userId + " does not exist"));
        Copy copy = copyRepository.findById(copyId).orElseThrow(() -> new IllegalStateException("Copy with ID " + copyId + " does not exist"));

        if (copy.getStatus() != CopyStatus.AVAILABLE && !reservationRepository.existsReservationByCopy_IdAndAndUser_IdAndStatus(copyId,userId,ReservationStatus.WAITING) ) {
            throw new IllegalAccessException("This copy isn't available");
        }

        copy.setStatus(CopyStatus.BORROWED);
        Loan loan = Loan.builder()
                .user(user)
                .copy(copy)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusWeeks(2))
                .build();
        loanRepository.save(loan);

        if(reservationRepository.existsReservationByCopy_Id(copyId)){
           Reservation reservation =  reservationRepository.findByCopy_IdAndStatus(copyId, ReservationStatus.WAITING).orElseThrow(() -> new IllegalStateException("There's no such reservation"));
           reservation.setStatus(ReservationStatus.CANCELLED);
           reservationRepository.save(reservation);
        }
    }

    @Transactional
    public void returnBook(Long loanId) {
        Loan loan = loanRepository.findByIdAndReturnDateIsNull(loanId)
                .orElseThrow(() -> new IllegalStateException("This book has already been returned or loan doesn't exist"));

        loan.setReturnDate(LocalDate.now());
        loan.getCopy().setStatus(CopyStatus.AVAILABLE);
    }

    public void extendLoan(LocalDate date, Long loanId) throws IllegalAccessException {
        Loan loan = loanRepository.findByIdAndReturnDateIsNull(loanId)
                .orElseThrow(() -> new IllegalStateException("This loan doesn't exist"));
        if (date.isBefore(loan.getEndDate())) {
            throw new IllegalAccessException("The new date must be after endDate");
        }
        loan.setEndDate(date);
        loanRepository.save(loan);
    }

}

