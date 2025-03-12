package com.example.library.repository;

import com.example.library.model.Copy;
import com.example.library.model.CopyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CopyRepository extends JpaRepository<Copy, Long> {
    List<Copy> findByLibraryId(Long libraryId);
    List<Copy> findByBookId(Long bookId);
    List<Copy> findCopyByBookIdAndStatus(Long bookId, CopyStatus status );
    List<Copy> findCopyByLibraryIdandStatus(Long libraryId, CopyStatus status );
    List<Copy> findCopyByStatus(CopyStatus status);
}
