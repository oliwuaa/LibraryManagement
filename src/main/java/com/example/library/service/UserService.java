package com.example.library.service;

import com.example.library.dto.UserDTO;
import com.example.library.dto.UserRegistrationDTO;
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

    public void addUser(UserRegistrationDTO user, Long libraryId) {
        if (userRepository.findByEmail(user.email()).isPresent()) {
            throw new IllegalStateException("Email already taken");
        }

        User newUser = new User();

        if (user.role() == UserRole.LIBRARIAN) {
            if (libraryId == null) {
                throw new IllegalArgumentException("Library ID is required for librarians");
            }

            Library library = libraryRepository.findById(libraryId)
                    .orElseThrow(() -> new IllegalArgumentException("Library not found"));
            newUser.setLibrary(library);
        } else {
            newUser.setLibrary(null);
        }


        newUser.setEmail(user.email());
        newUser.setPassword(user.password());
        newUser.setRole(user.role());
        if (user.name() != null) {
            newUser.setName(user.name());
        }
        if (user.surname() != null) {
            newUser.setSurname(user.surname());
        }

        userRepository.save(newUser);
    }


    public void updateUser(Long userId, UserDTO user) {
        User newUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User with ID " + userId + " does not exist"));

        if (user.name() != null && !user.name().isEmpty()) {
            newUser.setName(user.name());
        }

        if (user.surname() != null && !user.surname().isEmpty()) {
            newUser.setSurname(user.surname());
        }

        if (user.email() != null && !user.email().isEmpty()) {
            if (userRepository.findByEmail(user.email()).isPresent()) {
                throw new IllegalStateException("Email already taken");
            }
            newUser.setEmail(user.email());
        }

        userRepository.save(newUser);
    }

    public void changeRole(Long userId, UserRole role, Long libraryId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User with ID " + userId + " does not exist"));

        if (user.getRole() == role) {
            throw new IllegalStateException("User already has this role.");
        }

        if (role == UserRole.USER) {
            user.setRole(role);
            user.setLibrary(null);
        } else {
            if (libraryId == null) {
                throw new IllegalStateException("Library Id needed.");
            }

            Library library = libraryRepository.findById(libraryId)
                    .orElseThrow(() -> new IllegalArgumentException("Library not found"));

            user.setRole(role);
            user.setLibrary(library);
        }

        userRepository.save(user);
    }

    // TO BE CHANGED
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
