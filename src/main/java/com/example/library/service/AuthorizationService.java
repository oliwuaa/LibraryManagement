package com.example.library.service;

import com.example.library.model.*;
import com.example.library.repository.CopyRepository;
import com.example.library.repository.LoanRepository;
import com.example.library.repository.ReservationRepository;
import com.example.library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthorizationService {

    private final UserRepository userRepository;
    private final CopyRepository copyRepository;
    private final LoanRepository loanRepository;
    private final ReservationRepository reservationRepository;

    public String getEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    public boolean isLibrarianOfLibrary(Long libraryId) {
        String email = getEmail();

        User user = userRepository.findByEmailAndActiveTrue(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (user.getRole() != UserRole.LIBRARIAN) {
            return false;
        }

        return user.getLibrary() != null && user.getLibrary().getId().equals(libraryId);
    }

    public boolean isSelf(Long userID) {
        String email = getEmail();

        User user = userRepository.findByEmailAndActiveTrue(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return user.getId().equals(userID);
    }

    public boolean isCopyInLibrarianLibrary(Long copyId) {
        String email = getEmail();

        User librarian = userRepository.findByEmailAndActiveTrue(email)
                .orElseThrow(() -> new UsernameNotFoundException("Librarian not found"));

        if (librarian.getRole() != UserRole.LIBRARIAN || librarian.getLibrary() == null) {
            return false;
        }

        Copy copy = copyRepository.findById(copyId)
                .orElse(null);

        return copy != null && copy.getLibrary() != null && copy.getLibrary().getId().equals(librarian.getLibrary().getId());
    }

    public boolean isUserInLibrarianLibrary(Long userId) {
        String email = getEmail();

        User librarian = userRepository.findByEmailAndActiveTrue(email)
                .orElseThrow(() -> new UsernameNotFoundException("Librarian not found"));

        if (librarian.getRole() != UserRole.LIBRARIAN || librarian.getLibrary() == null) {
            return false;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<Reservation> reservations = reservationRepository.findAllByUserId(userId).stream()
                .filter(reservation -> reservation.getCopy().getLibrary().getId().equals(librarian.getLibrary().getId()))
                .toList();
        List<Loan> loans = loanRepository.findByUserId(userId).stream().filter(loan-> loan.getCopy().getLibrary().getId().equals(librarian.getLibrary().getId()))
                .toList();

        return !reservations.isEmpty() || !loans.isEmpty();
    }

    public boolean isLoanInLibrarianLibrary(Long loanId) {
        String email = getEmail();

        User librarian = userRepository.findByEmailAndActiveTrue(email)
                .orElseThrow(() -> new UsernameNotFoundException("Librarian not found"));

        if (librarian.getRole() != UserRole.LIBRARIAN || librarian.getLibrary() == null) {
            return false;
        }

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new UsernameNotFoundException("Loan not found"));

        return loan.getCopy().getLibrary().getId() != null && loan.getCopy().getLibrary().getId().equals(librarian.getLibrary().getId());
    }

    public boolean isUserLoan(Long loanId) {
        String email = getEmail();

        User user = userRepository.findByEmailAndActiveTrue(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (user.getRole() != UserRole.USER) {
            return false;
        }

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new UsernameNotFoundException("Loan not found"));

        return loan.getUser().getId() != null && loan.getUser().getId().equals(user.getId());
    }

    public boolean isReservationInLibrarianLibrary(Long reservationId) {
        String email = getEmail();

        User librarian = userRepository.findByEmailAndActiveTrue(email)
                .orElseThrow(() -> new UsernameNotFoundException("Librarian not found"));

        if (librarian.getRole() != UserRole.LIBRARIAN || librarian.getLibrary() == null) {
            return false;
        }

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new UsernameNotFoundException("Loan not found"));

        return reservation.getCopy().getLibrary().getId() != null && reservation.getCopy().getLibrary().getId().equals(librarian.getLibrary().getId());
    }

    public boolean isUserReservation(Long reservationId) {
        String email = getEmail();

        User user = userRepository.findByEmailAndActiveTrue(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (user.getRole() != UserRole.USER) {
            return false;
        }

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new UsernameNotFoundException("Loan not found"));

        return reservation.getUser().getId() != null && reservation.getUser().getId().equals(user.getId());
    }
}