package com.example.library.service;

import com.example.library.model.*;
import com.example.library.repository.CopyRepository;
import com.example.library.repository.LoanRepository;
import com.example.library.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthorizationService {

    private final UserService userService;
    private final CopyRepository copyRepository;
    private final LoanRepository loanRepository;
    private final ReservationRepository reservationRepository;

    public boolean isLibrarianOfLibrary(Long libraryId) {
        User currentUser = userService.getCurrentUser();
        if (currentUser.getRole() != UserRole.LIBRARIAN) {
            return false;
        }
        return currentUser.getLibrary() != null && currentUser.getLibrary().getId().equals(libraryId);
    }

    public boolean isSelf(Long userId) {
        User currentUser = userService.getCurrentUser();
        return currentUser.getId().equals(userId);
    }

    public boolean isCopyInLibrarianLibrary(Long copyId) {
        User librarian = userService.getCurrentUser();
        if (librarian.getRole() != UserRole.LIBRARIAN || librarian.getLibrary() == null) {
            return false;
        }
        Copy copy = copyRepository.findById(copyId).orElse(null);
        return copy != null && copy.getLibrary().getId().equals(librarian.getLibrary().getId());
    }

    public boolean isUserInLibrarianLibrary(Long userId) {
        User librarian = userService.getCurrentUser();
        if (librarian.getRole() != UserRole.LIBRARIAN || librarian.getLibrary() == null) {
            return false;
        }
        List<Reservation> reservations = reservationRepository.findAllByUserId(userId).stream()
                .filter(reservation -> reservation.getCopy().getLibrary().getId().equals(librarian.getLibrary().getId()))
                .toList();
        List<Loan> loans = loanRepository.findByUserId(userId).stream()
                .filter(loan -> loan.getCopy().getLibrary().getId().equals(librarian.getLibrary().getId()))
                .toList();
        return !reservations.isEmpty() || !loans.isEmpty();
    }


    public boolean isLoanInLibrarianLibrary(Long loanId) {
        User librarian = userService.getCurrentUser();
        if (librarian.getRole() != UserRole.LIBRARIAN || librarian.getLibrary() == null) {
            return false;
        }
        Loan loan = loanRepository.findById(loanId).orElseThrow(() -> new UsernameNotFoundException("Loan not found"));
        return loan.getCopy().getLibrary().getId().equals(librarian.getLibrary().getId());
    }

    public boolean isUserLoan(Long loanId) {
        User currentUser = userService.getCurrentUser();
        if (currentUser.getRole() != UserRole.USER) {
            return false;
        }
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new UsernameNotFoundException("Loan not found"));
        return loan.getUser().getId().equals(currentUser.getId());
    }


    public boolean isReservationInLibrarianLibrary(Long reservationId) {
        User librarian = userService.getCurrentUser();
        if (librarian.getRole() != UserRole.LIBRARIAN || librarian.getLibrary() == null) {
            return false;
        }
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new UsernameNotFoundException("Reservation not found"));
        return reservation.getCopy().getLibrary().getId().equals(librarian.getLibrary().getId());
    }


    public boolean isUserReservation(Long reservationId) {
        User currentUser = userService.getCurrentUser();
        if (currentUser.getRole() != UserRole.USER) {
            return false;
        }
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new UsernameNotFoundException("Reservation not found"));
        return reservation.getUser().getId().equals(currentUser.getId());
    }
}