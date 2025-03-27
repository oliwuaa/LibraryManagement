package com.example.library.service;

import com.example.library.model.*;
import com.example.library.repository.*;
import com.example.library.specification.LibrarySpecification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

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

    public List<Library> getAllLibraries() {
        return libraryRepository.findAll();
    }

    public Library getLibraryById(Long libraryId) {
        return libraryRepository.findById(libraryId).orElseThrow(() -> new RuntimeException("Library not found"));
    }

    public List<Library> getLibrariesByStatus(LibraryStatus status) {
        return libraryRepository.findByStatus(status);
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

    public void addLibrary(Library newLibrary) throws IllegalAccessException {
        if (libraryRepository.findByAddress(newLibrary.getAddress()).isPresent())
            throw new IllegalAccessException("This library has already exist");
        newLibrary.setStatus(LibraryStatus.ACTIVE);
        libraryRepository.save(newLibrary);

    }

    public List<User> getLibrariansForLibrary(Long libraryId) {
        Library library = libraryRepository.findById(libraryId)
                .orElseThrow(() -> new IllegalStateException("Library with id " + libraryId + " not found"));

        return userRepository.findByRoleAndLibraryId(UserRole.LIBRARIAN, libraryId);
    }

    public void deleteLibrary(Long libraryId) throws IllegalStateException {


        Library library = libraryRepository.findById(libraryId)
                .orElseThrow(() -> new RuntimeException("Library with ID " + libraryId + " doesn't exist."));

        List<Copy> copies = copyRepository.findByLibraryId(libraryId);
        boolean hasActiveLoansOrReservations = false;

        for (Copy copy : copies) {
            if (loanRepository.existsLoanByCopy_Id(copy.getId()) || reservationRepository.existsReservationByCopy_Id(copy.getId())) {
                copy.setStatus(CopyStatus.REMOVED);
                copyRepository.save(copy);
                hasActiveLoansOrReservations = true;
            } else copyRepository.delete(copy);
        }

        userRepository.findByRoleAndLibraryId(UserRole.LIBRARIAN, libraryId)
                .forEach(user -> {
                    user.setRole(UserRole.USER);
                    user.setLibrary(null);
                    userRepository.save(user);
                });

        if (hasActiveLoansOrReservations) {
            library.setStatus(LibraryStatus.CLOSED);
            libraryRepository.save(library);
        } else {
            libraryRepository.delete(library);
        }

    }

    @Transactional
    public void updateLibrary(Long libraryId, Library library) throws IllegalStateException {
        Library changedLibrary = libraryRepository.findById(libraryId).orElseThrow(() -> new IllegalStateException("Library with ID " + libraryId + " does not exist"));

        String name = library.getName();
        String address = library.getAddress();
        if (name != null && name.length() > 0 && !Objects.equals(changedLibrary.getName(), name)) {
            changedLibrary.setName(name);
        }

        if (address != null && address.length() > 0 && !Objects.equals(changedLibrary.getAddress(), address)) {
            changedLibrary.setAddress(address);
        }
    }

}
