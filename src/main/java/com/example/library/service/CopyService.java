package com.example.library.service;

import com.example.library.model.Book;
import com.example.library.model.Copy;
import com.example.library.model.CopyStatus;
import com.example.library.model.Library;
import com.example.library.repository.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CopyService {

    private final CopyRepository copyRepository;
    private final LibraryRepository libraryRepository;
    private final BookRepository bookRepository;
    private final LoanRepository loanRepository;
    private final ReservationRepository reservationRepository;

    public List<Copy> getAllCopies() {
        return copyRepository.findAll();
    }

    public List<Copy> getActiveCopies() {
        return copyRepository.findByStatusIn(List.of(CopyStatus.AVAILABLE, CopyStatus.BORROWED));
    }

    public Copy getCopyById(Long copyId) {
        return copyRepository.findById(copyId).orElseThrow(() -> new IllegalStateException("Copy with ID " + copyId + " does not exist"));
    }

    public List<Copy> getCopiesByLibrary(Long libraryId) {
        return copyRepository.findByLibraryId(libraryId);
    }

    public List<Copy> getAvailableCopiesByLibrary(Long libraryId) {
        return copyRepository.findByLibraryIdAndStatus(libraryId, CopyStatus.AVAILABLE);
    }

    public List<Copy> getCopiesByBook(Long bookId) {
        return copyRepository.findByBookId(bookId);
    }

    public List<Copy> getAvailableCopiesByBook(Long bookId) {
        return copyRepository.findByBookIdAndStatus(bookId, CopyStatus.AVAILABLE);
    }

    public List<Copy> getAvailableCopies() {
        return copyRepository.findByStatus(CopyStatus.AVAILABLE);
    }

    @Transactional
    public void addCopy(Long bookId, Long libraryId) throws IllegalAccessException {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Book with ID " + bookId + " does not exist"));
        Library library = libraryRepository.findById(libraryId)
                .orElseThrow(() -> new EntityNotFoundException("Library with ID " + libraryId + " does not exist"));

        Copy copy = Copy.builder()
                .book(book)
                .library(library)
                .status(CopyStatus.AVAILABLE)
                .build();
        copyRepository.save(copy);

    }

    public void updateCopyStatus(Long copyId, CopyStatus status) {
        Copy copy = copyRepository.findById(copyId).orElseThrow(() -> new IllegalStateException("Copy with ID " + copyId + " does not exist"));
        copy.setStatus(status);
        copyRepository.save(copy);
    }

    public void deleteCopy(Long copyId) {
        Copy copy = copyRepository.findById(copyId).orElseThrow(() -> new IllegalStateException("Copy with ID " + copyId + " does not exist"));

        if (copy.getStatus() == CopyStatus.AVAILABLE) {
            if (loanRepository.existsLoanByCopy_Id(copyId) || reservationRepository.existsReservationByCopy_Id(copyId)) {
                copy.setStatus(CopyStatus.REMOVED);
                copyRepository.save(copy);
            } else copyRepository.delete(copy);
        }
    }
}
