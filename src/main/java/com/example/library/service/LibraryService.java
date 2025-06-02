package com.example.library.service;

import com.example.library.dto.LibraryDTO;
import com.example.library.exception.BadRequestException;
import com.example.library.exception.NotFoundException;
import com.example.library.model.*;
import com.example.library.repository.*;
import com.example.library.specification.LibrarySpecification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class LibraryService {
    private final LibraryRepository libraryRepository;
    private final UserRepository userRepository;
    private final CopyRepository copyRepository;
    private final LoanRepository loanRepository;
    private final ReservationRepository reservationRepository;
    private final UserService userService;
    private final Clock clock;

    public List<Library> getAllLibraries() {
        return libraryRepository.findAll();
    }

    public Library getLibraryById(Long libraryId) {
        return libraryRepository.findById(libraryId).orElseThrow(() -> new NotFoundException("Library with ID " + libraryId + " not found"));
    }

    public Library getLibraryForCurrentLibrarian() {
        User user = userService.getCurrentUser();

        if (user.getRole() != UserRole.LIBRARIAN) {
            throw new BadRequestException("User is not a librarian");
        }

        if (user.getLibrary() == null) {
            throw new NotFoundException("Librarian is not assigned to any library");
        }

        return user.getLibrary();
    }



    public List<Library> searchLibraries(String name, String address) {
        Specification<Library> spec = Specification.where(null);

        if (name != null && !name.isBlank()) {
            spec = spec.and(LibrarySpecification.hasNameLike(name));
        }

        if (address != null && !address.isBlank()) {
            spec = spec.and(LibrarySpecification.hasAddressLike(address));
        }

        return libraryRepository.findAll(spec);
    }

    public void addLibrary(LibraryDTO library) {
        if (libraryRepository.findByAddress(library.address()).isPresent())
            throw new BadRequestException("This library already exists");

        Library newLibrary = new Library();
        newLibrary.setStatus(LibraryStatus.ACTIVE);
        newLibrary.setName(library.name());
        newLibrary.setAddress(library.address());
        libraryRepository.save(newLibrary);
    }

    public void endLoan(Long copyId) {
        loanRepository.findLoanByCopy_Id(copyId)
                .ifPresent(loan -> {
                    loan.setReturnDate(LocalDate.now(clock));
                    loanRepository.save(loan);
                });
    }

    public void endReservation(Long copyId) {
        reservationRepository.findReservationByCopy_Id(copyId)
                .ifPresent(reservation -> {
                    reservation.setStatus(ReservationStatus.CANCELLED);
                    reservationRepository.save(reservation);
                });
    }

    public boolean deleteLibrary(Long libraryId) {
        Library library = libraryRepository.findById(libraryId)
                .orElseThrow(() -> new NotFoundException("Library with ID " + libraryId + " doesn't exist."));

        List<Copy> copies = copyRepository.findByLibraryId(libraryId);
        boolean hasActiveLoansOrReservations = false;

        for (Copy copy : copies) {
            if (loanRepository.existsLoanByCopy_Id(copy.getId()) || reservationRepository.existsReservationByCopy_Id(copy.getId())) {
                copy.setStatus(CopyStatus.REMOVED);
                copyRepository.save(copy);
                endLoan(copy.getId());
                endReservation(copy.getId());
                hasActiveLoansOrReservations = true;
            } else {
                copyRepository.delete(copy);
            }
        }

        userRepository.findByRoleAndLibraryIdAndActiveTrue(UserRole.LIBRARIAN, libraryId)
                .forEach(user -> {
                    user.setRole(UserRole.USER);
                    user.setLibrary(null);
                    userRepository.save(user);
                });

        if (hasActiveLoansOrReservations) {
            library.setStatus(LibraryStatus.CLOSED);
            libraryRepository.save(library);
            return false; // closed
        } else {
            libraryRepository.delete(library);
            return true; // deleted
        }
    }

    @Transactional
    public void updateLibrary(Long libraryId, LibraryDTO library) {
        if ((library.name() == null || library.name().isBlank()) &&
                (library.address() == null || library.address().isBlank())) {
            throw new BadRequestException("At least one field (name or address) must be provided for update.");
        }

        Library changedLibrary = libraryRepository.findById(libraryId)
                .orElseThrow(() -> new NotFoundException("Library with ID " + libraryId + " does not exist"));

        String name = library.name();
        String address = library.address();

        if (name != null && !name.isBlank() && !Objects.equals(changedLibrary.getName(), name)) {
            changedLibrary.setName(name);
        }

        if (address != null && !address.isBlank() && !Objects.equals(changedLibrary.getAddress(), address)) {
            changedLibrary.setAddress(address);
        }
        libraryRepository.save(changedLibrary);
    }

}