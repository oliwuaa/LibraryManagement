package com.example.library.repository;

import com.example.library.model.CopyStatus;
import com.example.library.model.Library;
import com.example.library.model.LibraryStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LibraryRepository extends JpaRepository<Library, Long> {
    Optional<Library> findLibraryByName(String name);
    Optional<Library> findLibraryByAddress(String location);
    List<Library> findLibraryByStatus(LibraryStatus status);
    List<Library> findByStatusIn(List<LibraryStatus> statuses);
}
