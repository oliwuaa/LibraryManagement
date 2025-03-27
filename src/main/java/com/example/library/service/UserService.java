package com.example.library.service;

import com.example.library.model.Library;
import com.example.library.model.User;
import com.example.library.model.UserRole;
import com.example.library.repository.LibraryRepository;
import com.example.library.repository.UserRepository;
import com.example.library.specification.UserSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final LibraryRepository libraryRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new IllegalStateException("User with ID " + userId + " does not exist"));
    }

    public List<User> getUsersByRole(UserRole role) {
        return userRepository.findByRole(role);
    }

    public List<User> getLibrariansFromLibrary(Long libraryId) {
        return userRepository.findByRoleAndLibraryId(UserRole.LIBRARIAN, libraryId);
    }

    public void addUser(User newUser, Long libraryId) {
        if (userRepository.findByEmail(newUser.getEmail()).isPresent()) {
            throw new IllegalStateException("Email already taken");
        }

        if (newUser.getRole() == UserRole.LIBRARIAN) {
            if (libraryId == null) {
                throw new IllegalArgumentException("Library ID is required for librarians");
            }

            Library library = libraryRepository.findById(libraryId)
                    .orElseThrow(() -> new IllegalArgumentException("Library not found"));
            newUser.setLibrary(library);
        } else {
            newUser.setLibrary(null);
        }

        userRepository.save(newUser);
    }


    public void updateUser(Long userId, User updatedUser) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User with ID " + userId + " does not exist"));

        if (updatedUser.getName() != null && !updatedUser.getName().isEmpty()) {
            user.setName(updatedUser.getName());
        }

        if (updatedUser.getSurname() != null && !updatedUser.getSurname().isEmpty()) {
            user.setSurname(updatedUser.getSurname());
        }

        if (updatedUser.getEmail() != null && !updatedUser.getEmail().isEmpty()) {
            if (userRepository.findByEmail(updatedUser.getEmail()).isPresent()) {
                throw new IllegalStateException("Email already taken");
            }
            user.setEmail(updatedUser.getEmail());
        }

        userRepository.save(user);
    }

    public void changeRole(Long userId, UserRole role, Long libraryId ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User with ID " + userId + " does not exist"));

        if(user.getRole()==role){
            throw new IllegalStateException("User already has this role.");
        }

        if(role == UserRole.USER) {
            user.setRole(role);
            user.setLibrary(null);
        }
        else {
            if(libraryId==null){
                throw new IllegalStateException("Library Id needed.");
            }

            Library library = libraryRepository.findById(libraryId)
                    .orElseThrow(() -> new IllegalArgumentException("Library not found"));

            user.setRole(role);
            user.setLibrary(library);
        }

        userRepository.save(user);
    }

    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalStateException("User not found");
        }
        userRepository.deleteById(userId);
    }

    public List<User> searchUsers(String name, String email, UserRole role) {
        Specification<User> spec = Specification.where(null);

        if (name != null && !name.isBlank()) {
            spec = spec.and(UserSpecification.hasNameLike(name));
        }

        if (email != null && !email.isBlank()) {
            spec = spec.and(UserSpecification.hasEmailLike(email));
        }

        if (role != null) {
            spec = spec.and(UserSpecification.hasRole(role));
        }

        return userRepository.findAll(spec);
    }
}
