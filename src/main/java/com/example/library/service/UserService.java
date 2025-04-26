package com.example.library.service;

import com.example.library.dto.UserDTO;
import com.example.library.dto.UserInfoDTO;
import com.example.library.dto.UserRegistrationDTO;
import com.example.library.exception.BadRequestException;
import com.example.library.exception.NotFoundException;
import com.example.library.model.Library;
import com.example.library.model.User;
import com.example.library.model.UserRole;
import com.example.library.repository.LibraryRepository;
import com.example.library.repository.UserRepository;
import com.example.library.specification.UserSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.util.List;


@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final LibraryRepository libraryRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UserInfoDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> new UserInfoDTO(
                        user.getId(),
                        user.getEmail(),
                        user.getName(),
                        user.getSurname(),
                        user.getRole().name(),
                        user.getLibrary() != null ? user.getLibrary().getId() : null
                ))
                .toList();
    }

    public UserInfoDTO getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with ID " + userId + " does not exist"));

        return new UserInfoDTO(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getSurname(),
                user.getRole().name(),
                user.getLibrary() != null ? user.getLibrary().getId() : null
        );
    }

    public List<UserInfoDTO> getUsersByRole(UserRole role) {

        return userRepository.findByRole(role).stream()
                .map(user -> new UserInfoDTO(
                        user.getId(),
                        user.getEmail(),
                        user.getName(),
                        user.getSurname(),
                        user.getRole().name(),
                        user.getLibrary() != null ? user.getLibrary().getId() : null
                ))
                .toList();
    }

    public List<UserInfoDTO> getLibrariansFromLibrary(Long libraryId) {
        if (!libraryRepository.existsById(libraryId)) {
            throw new NotFoundException("Library with ID " + libraryId + " does not exist");
        }
        return userRepository.findByRoleAndLibraryId(UserRole.LIBRARIAN, libraryId).stream()
                .map(user -> new UserInfoDTO(
                        user.getId(),
                        user.getEmail(),
                        user.getName(),
                        user.getSurname(),
                        user.getRole().name(),
                        user.getLibrary() != null ? user.getLibrary().getId() : null
                ))
                .toList();
    }

    public void addUser(UserRegistrationDTO user, Long libraryId) {
        if (userRepository.findByEmail(user.email()).isPresent()) {
            throw new BadRequestException("Email already taken");
        }

        User newUser = new User();

        if (user.role() == UserRole.LIBRARIAN) {
            if (libraryId == null) {
                throw new BadRequestException("Library ID is required for librarians");
            }

            Library library = libraryRepository.findById(libraryId)
                    .orElseThrow(() -> new NotFoundException("Library with ID " + libraryId + " does not exist"));
            newUser.setLibrary(library);
        } else {
            newUser.setLibrary(null);
        }


        newUser.setEmail(user.email());
        newUser.setPassword(passwordEncoder.encode(user.password()));
        newUser.setRole(user.role());
        if (user.name() != null) {
            newUser.setName(user.name());
        }
        if (user.surname() != null) {
            newUser.setSurname(user.surname());
        }

        System.out.println("Saving user: " + newUser);
        userRepository.save(newUser);
    }


    public void updateUser(Long userId, UserDTO user) {
        User newUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with ID " + userId + " does not exist"));

        if (user.name() != null && !user.name().isEmpty()) {
            newUser.setName(user.name());
        }

        if (user.surname() != null && !user.surname().isEmpty()) {
            newUser.setSurname(user.surname());
        }

        if (user.email() != null && !user.email().isEmpty()) {
            if (userRepository.findByEmail(user.email()).isPresent()) {
                throw new BadRequestException("Email already taken");
            }
            newUser.setEmail(user.email());
        }

        userRepository.save(newUser);
    }

    public void changeRole(Long userId, UserRole role, Long libraryId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with ID " + userId + " does not exist"));

        if (user.getRole() == role) {
            throw new BadRequestException("User already has this role.");
        }

        if (role == UserRole.USER || role == UserRole.ADMIN) {
            user.setRole(role);
            user.setLibrary(null);
        } else {
            if (libraryId == null) {
                throw new BadRequestException("Library Id needed.");
            }

            Library library = libraryRepository.findById(libraryId)
                    .orElseThrow(() -> new NotFoundException("Library not found"));

            user.setRole(role);
            user.setLibrary(library);
        }
        userRepository.save(user);
    }

    // TO BE CHANGED
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with ID " + userId + " does not exist");
        }
        userRepository.deleteById(userId);
    }

    public List<UserInfoDTO> searchUsers(String name, String email, UserRole role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String mail = authentication.getName();

        User currentUser = userRepository.findByEmail(mail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

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

        if (currentUser.getRole() == UserRole.LIBRARIAN) {
            return userRepository.findAll(spec).stream()
                    .filter(r -> r.getLibrary().getId()
                            .equals(currentUser.getLibrary().getId()))
                    .map(user -> new UserInfoDTO(
                            user.getId(),
                            user.getEmail(),
                            user.getName(),
                            user.getSurname(),
                            user.getRole().name(),
                            user.getLibrary() != null ? user.getLibrary().getId() : null
                    ))
                    .toList();
        }
        return userRepository.findAll(spec).stream()
                .map(user -> new UserInfoDTO(
                        user.getId(),
                        user.getEmail(),
                        user.getName(),
                        user.getSurname(),
                        user.getRole().name(),
                        user.getLibrary() != null ? user.getLibrary().getId() : null
                ))
                .toList();
    }
}