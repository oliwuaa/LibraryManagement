package com.example.library.repository;

import com.example.library.model.User;
import com.example.library.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    List<User> findUsersByRole(UserRole role);
    List<User> findLibrarians(UserRole role, Long libraryId);


}
