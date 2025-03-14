package com.example.library.repository;

import com.example.library.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long > {
    Optional<Book> findBookByIsbn(String isbn);
    boolean existsBookByIsbn(String isbn);
    List<Book> findBookByTitle(String title);
    List<Book> findBookByAuthor(String author);

}
