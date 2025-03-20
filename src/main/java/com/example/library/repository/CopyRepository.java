package com.example.library.repository;

import com.example.library.model.Book;
import com.example.library.model.Copy;
import com.example.library.model.CopyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CopyRepository extends JpaRepository<Copy, Long> {
    List<Copy> findByLibraryId(Long libraryId);
    List<Copy> findByBookId(Long bookId);
    List<Copy> findByBookIdAndStatus(Long bookId, CopyStatus status);
    List<Copy> findByLibraryIdAndStatus(Long libraryId, CopyStatus status);
    List<Copy> findByStatus(CopyStatus status);
    List<Copy> findByStatusIn(List<CopyStatus> statuses);
    boolean existsByBook(Book book);
}
