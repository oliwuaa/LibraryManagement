package com.example.library.specification;

import com.example.library.model.User;
import com.example.library.model.UserRole;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {

    public static Specification<User> hasNameLike(String name) {
        return (root, query, builder) -> builder.like(builder.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    public static Specification<User> hasEmailLike(String email) {
        return (root, query, builder) -> builder.like(builder.lower(root.get("email")), "%" + email.toLowerCase() + "%");
    }

    public static Specification<User> hasRole(UserRole role) {
        return (root, query, builder) -> {
            if (role == null) {
                return builder.conjunction();
            }
            return builder.equal(root.get("role"), role);
        };
    }

}
