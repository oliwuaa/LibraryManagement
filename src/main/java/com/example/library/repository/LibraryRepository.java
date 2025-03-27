package com.example.library.repository;

import com.example.library.model.Library;
import com.example.library.model.LibraryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface LibraryRepository extends JpaRepository<Library, Long>, JpaSpecificationExecutor<Library> {
    Optional<Library> findByName(String name);

    Optional<Library> findByAddress(String location);

    List<Library> findByStatus(LibraryStatus status);

}
