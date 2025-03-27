package com.example.library.specification;

import com.example.library.model.Book;
import org.springframework.data.jpa.domain.Specification;

public class BookSpecification {

    public static Specification<Book> hasTitle(String title) {
        return (root, query, criteriaBuilder) ->
                (title == null || title.isEmpty()) ? criteriaBuilder.conjunction() :
                        criteriaBuilder.equal(root.get("title"), title);
    }

    public static Specification<Book> hasAuthor(String author) {
        return (root, query, criteriaBuilder) ->
                (author == null || author.isEmpty()) ? criteriaBuilder.conjunction() :
                        criteriaBuilder.equal(root.get("author"), author);
    }

    public static Specification<Book> hasIsbn(String isbn) {
        return (root, query, criteriaBuilder) ->
                (isbn == null || isbn.isEmpty()) ? criteriaBuilder.conjunction() :
                        criteriaBuilder.equal(root.get("isbn"), isbn);
    }
}
