package com.example.library.repository;

import com.example.library.model.User;
import com.example.library.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmailAndActiveTrue(String email);
    List<User> findAllByActiveTrue();
    List<User> findByRoleAndActiveTrue(UserRole role);
    List<User> findByRoleAndLibraryIdAndActiveTrue(UserRole role, Long libraryId);
    long countByRoleAndActiveTrue(UserRole role);
}