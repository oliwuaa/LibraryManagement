package com.example.library.repository;

import com.example.library.model.Library;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LibraryRepository extends JpaRepository<Library, Long> {
    Optional<Library> findLibraryByName(String name);
    Optional<Library> findLibraryByAddress(String location);
}
