package com.example.library.repository;
import com.example.library.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByUserId(Long userId);

    List<Loan> findByUserIdAndReturnDateIsNullL(Long userId);

    Optional<Loan> findByUserAndCopyAndReturnDateIsNull(Long userId, Long copyId);


}
