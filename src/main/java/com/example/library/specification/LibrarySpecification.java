package com.example.library.specification;

import com.example.library.model.Library;
import org.springframework.data.jpa.domain.Specification;

public class LibrarySpecification {
    public static Specification<Library> hasNameLike(String name) {
        return (root, query, builder) -> builder.like(builder.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }
    public static Specification<Library> hasAddressLike(String address) {
        return (root, query, builder) -> builder.like(builder.lower(root.get("address")), "%" + address.toLowerCase() + "%");
    }
}
