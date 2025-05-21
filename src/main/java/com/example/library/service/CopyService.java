package com.example.library.service;

import com.example.library.exception.BadRequestException;
import com.example.library.exception.NotFoundException;
import com.example.library.model.*;
import com.example.library.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CopyService {

    private final CopyRepository copyRepository;
    private final UserRepository userRepository;
    private final LibraryRepository libraryRepository;
    private final BookRepository bookRepository;
    private final LoanRepository loanRepository;
    private final ReservationRepository reservationRepository;

    public List<Copy> getAllCopies() {
        return copyRepository.findAll();
    }

    public Copy getCopyById(Long copyId) {
        return copyRepository.findById(copyId).orElseThrow(() -> new NotFoundException("Copy with ID " + copyId + " does not exist"));
    }

    public List<Copy> getCopiesByLibrary(Long libraryId) {
        if (!libraryRepository.existsById(libraryId)) {
            throw new NotFoundException("Library with ID " + libraryId + " does not exist");
        }
        return copyRepository.findByLibraryId(libraryId);
    }

    public List<Copy> getAvailableCopiesByLibrary(Long libraryId) {
        return copyRepository.findByLibraryIdAndStatus(libraryId, CopyStatus.AVAILABLE);
    }

    public List<Copy> getCopiesByBook(Long bookId) {
        if (!bookRepository.existsById(bookId)) {
            throw new NotFoundException("Book with ID " + bookId + " does not exist");
        }
        return copyRepository.findByBookId(bookId);
    }

    public List<Copy> getAvailableCopiesByBook(Long bookId) {
        return copyRepository.findByBookIdAndStatus(bookId, CopyStatus.AVAILABLE);
    }

    public List<Copy> getAvailableCopiesOfBookByLibrary(Long bookId, Long LibraryId) {
        return copyRepository.findByBookIdAndLibraryId(bookId,LibraryId);
    }

    public List<Copy> getAvailableCopies() {
        return copyRepository.findByStatus(CopyStatus.AVAILABLE);
    }

    @Transactional
    public void addCopy(Long bookId, Long libraryId) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userRepository.findByEmailAndActiveTrue(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new NotFoundException("Book with ID " + bookId + " does not exist"));

        Library library = libraryRepository.findById(libraryId)
                .orElseThrow(() -> new NotFoundException("Library with ID " + libraryId + " does not exist"));

        Copy copy = Copy.builder()
                .book(book)
                .library(library)
                .status(CopyStatus.AVAILABLE)
                .build();
        copyRepository.save(copy);
    }

    public void updateCopyStatus(Long copyId, CopyStatus status) {
        Copy copy = copyRepository.findById(copyId)
                .orElseThrow(() -> new NotFoundException("Copy with ID " + copyId + " does not exist"));

        if (copy.getStatus() == status) {
            throw new BadRequestException("Copy already has this status.");
        }

        copy.setStatus(status);
        copyRepository.save(copy);
    }

    public void deleteCopy(Long copyId) {
        Copy copy = copyRepository.findById(copyId)
                .orElseThrow(() -> new NotFoundException("Copy with ID " + copyId + " does not exist"));

        if (copy.getStatus() == CopyStatus.AVAILABLE) {
            if (loanRepository.existsLoanByCopy_Id(copyId) || reservationRepository.existsReservationByCopy_Id(copyId)) {
                copy.setStatus(CopyStatus.REMOVED);
                copyRepository.save(copy);
            } else {
                copyRepository.delete(copy);
            }
        } else {
            throw new BadRequestException("Cannot delete copy – it is currently borrowed, reserved, or already removed.");
        }
    }
}