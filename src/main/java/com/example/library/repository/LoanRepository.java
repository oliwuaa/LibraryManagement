package com.example.library.repository;

import com.example.library.model.Loan;
import com.example.library.model.Reservation;
import com.example.library.model.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByUserId(Long userId);
    List<Loan> findByUserIdAndReturnDateIsNull(Long userId);
    Optional<Loan> findByUserIdAndCopyIdAndReturnDateIsNull(Long userId, Long copyId);
    boolean existsLoanByCopy_Id(Long copyId);
    Optional<Loan> findLoanByCopy_Id(Long copyId);
    Optional<Loan> findByIdAndReturnDateIsNull(Long loanId);
    List<Loan> findByEndDateBeforeAndReturnDateIsNull(LocalDate dueDate);
    List<Loan> findByCopy_Library_Id(Long libraryId);
    boolean existsByUserIdAndReturnDateIsNull(Long userId);
}