package com.example.library.service;

import com.example.library.model.Book;
import com.example.library.model.Copy;
import com.example.library.model.CopyStatus;
import com.example.library.model.Library;
import com.example.library.repository.BookRepository;
import com.example.library.repository.CopyRepository;
import com.example.library.repository.LibraryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CopyService {
    @Autowired
    private final CopyRepository copyRepository;
    @Autowired
    private final LibraryRepository libraryRepository;
    @Autowired
    private final BookRepository bookRepository;

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
        return copyRepository.findCopyByLibraryIdAndStatus(libraryId, CopyStatus.AVAILABLE);
    }

    public List<Copy> getCopiesByBook(Long bookId) {
        return copyRepository.findByBookId(bookId);
    }

    public List<Copy> getAvailableCopiesByBook(Long bookId) {
        return copyRepository.findCopyByBookIdAndStatus(bookId, CopyStatus.AVAILABLE);
    }

    public List<Copy> getAvailableCopies() {
        return copyRepository.findCopyByStatus(CopyStatus.AVAILABLE);
    }

    public void addCopy(Long bookId, Long libraryId) throws IllegalAccessException {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalAccessException("Book with ID " + bookId + " does not exist"));
        Library library = libraryRepository.findById(libraryId)
                .orElseThrow(() -> new IllegalAccessException("Library with ID " + libraryId + " does not exist"));

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
    }

    public void deleteCopy(Long copyId) throws IllegalAccessException {
        Copy copy = copyRepository.findById(copyId).orElseThrow(() -> new IllegalStateException("Copy with ID " + copyId + " does not exist"));
        copy.setStatus(CopyStatus.REMOVED);
    }
}
