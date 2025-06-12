package com.example.library.service;

import com.example.library.model.ReservationStatus;
import com.example.library.dto.UserInfoDTO;
import com.example.library.dto.UserRegistrationDTO;
import com.example.library.exception.BadRequestException;
import com.example.library.exception.NotFoundException;
import com.example.library.model.Library;
import com.example.library.model.User;
import com.example.library.model.UserRole;
import com.example.library.repository.LibraryRepository;
import com.example.library.repository.LoanRepository;
import com.example.library.repository.ReservationRepository;
import com.example.library.repository.UserRepository;
import com.example.library.specification.UserSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
@RequiredArgsConstructor
public class UserService {
    private final LoanRepository loanRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final LibraryRepository libraryRepository;
    private final PasswordEncoder passwordEncoder;
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        log.info("Aktualnie zalogowany użytkownik: {}", email);
        return userRepository.findByEmailAndActiveTrue(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

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

    public List<UserInfoDTO> getActiveUsers() {
        return userRepository.findAllByActiveTrue().stream()
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

    public List<User> getUsersRelatedToLibrarianLibrary() {
        User librarian = getCurrentUser();
        if (librarian.getRole() != UserRole.LIBRARIAN || librarian.getLibrary() == null) {
            throw new AccessDeniedException("Only librarians with library assigned can access this");
        }

        Long libraryId = librarian.getLibrary().getId();
        List<User> allUsers = userRepository.findAll();

        return allUsers.stream()
                .filter(user -> {
                    boolean hasReservation = reservationRepository.findAllByUserId(user.getId()).stream()
                            .anyMatch(res -> res.getCopy().getLibrary().getId().equals(libraryId));

                    boolean hasLoan = loanRepository.findByUserId(user.getId()).stream()
                            .anyMatch(loan -> loan.getCopy().getLibrary().getId().equals(libraryId));

                    return hasReservation || hasLoan;
                })
                .toList();
    }


    public User getUserByEmail(String email) {
        return userRepository.findByEmailAndActiveTrue(email)
                .orElseThrow(() -> new NotFoundException("User with email " + email + " does not exist"));
    }


    public List<UserInfoDTO> getUsersByRole(UserRole role) {

        return userRepository.findByRoleAndActiveTrue(role).stream()
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
        return userRepository.findByRoleAndLibraryIdAndActiveTrue(UserRole.LIBRARIAN, libraryId).stream()
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

    public void addUser(UserRegistrationDTO user) {
        if (userRepository.findByEmailAndActiveTrue(user.email()).isPresent()) {
            throw new BadRequestException("Email already taken");
        }

        User newUser = new User();
        newUser.setLibrary(null);

        newUser.setEmail(user.email());
        newUser.setPassword(passwordEncoder.encode(user.password()));
        newUser.setRole(UserRole.USER);
        if (user.name() != null) {
            newUser.setName(user.name());
        }
        if (user.surname() != null) {
            newUser.setSurname(user.surname());
        }

        System.out.println("Saving user: " + newUser);
        userRepository.save(newUser);
    }


    public void updateUser(Long userId, UserInfoDTO user) {
        User newUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with ID " + userId + " does not exist"));

        if (user.name() != null && !user.name().isBlank()) {
            newUser.setName(user.name());
        }

        if (user.surname() != null && !user.surname().isBlank()) {
            newUser.setSurname(user.surname());
        }

        if (user.email() != null && !user.email().isBlank()) {
            Optional<User> existingUserWithEmail = userRepository.findByEmailAndActiveTrue(user.email());
            if (existingUserWithEmail.isPresent() && !existingUserWithEmail.get().getId().equals(userId)) {
                throw new BadRequestException("Email already taken");
            }
            newUser.setEmail(user.email());
        }

        if (user.role() != null && !user.role().isBlank()) {
            try {
                UserRole newRole = UserRole.valueOf(user.role().toUpperCase());
                newUser.setRole(newRole);

                if (newRole == UserRole.LIBRARIAN) {
                    if (user.libraryId() == null) {
                        throw new BadRequestException("Library ID must be provided for role LIBRARIAN");
                    }

                    Library library = libraryRepository.findById(user.libraryId())
                            .orElseThrow(() -> new NotFoundException("Library with ID " + user.libraryId() + " does not exist"));

                    newUser.setLibrary(library);
                } else {
                    newUser.setLibrary(null);
                }

            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid role: " + user.role());
            }
        }

        userRepository.save(newUser);
    }


    public void changeRole(Long userId, UserRole role, Long libraryId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with ID " + userId + " does not exist"));

        if (user.getRole() == role) {
            throw new BadRequestException("User already has this role.");
        }

        if (user.getRole() == UserRole.ADMIN && role != UserRole.ADMIN) {
            long adminCount = userRepository.countByRoleAndActiveTrue(UserRole.ADMIN);
            if (adminCount <= 1) {
                throw new BadRequestException("Cannot change role. At least one admin must remain.");
            }
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

    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (user.getRole() == UserRole.ADMIN) {
            long adminCount = userRepository.countByRoleAndActiveTrue(UserRole.ADMIN);
            if (adminCount <= 1) {
                throw new BadRequestException("Cannot delete the last admin.");
            }
        }

        if (reservationRepository.existsByUserIdAndStatus(userId, ReservationStatus.WAITING) || loanRepository.existsByUserIdAndReturnDateIsNull(userId)) {
            throw new IllegalStateException("Cannot delete user with active reservations or loans.");
        }
        user.setActive(false);
        userRepository.save(user);
    }

    public List<UserInfoDTO> searchUsers(String name, String email, UserRole role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String mail = authentication.getName();

        User currentUser = userRepository.findByEmailAndActiveTrue(mail)
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

        List<User> users = userRepository.findAll(spec);

        if (currentUser.getRole() == UserRole.LIBRARIAN) {

            if (role == UserRole.LIBRARIAN || role == UserRole.ADMIN) {
                Long libraryId = currentUser.getLibrary() != null ? currentUser.getLibrary().getId() : null;
                users = users.stream()
                        .filter(user -> user.getLibrary() != null &&
                                user.getLibrary().getId().equals(libraryId))
                        .collect(Collectors.toList());
            }
        }

        return users.stream()
                .map(user -> new UserInfoDTO(
                        user.getId(),
                        user.getEmail(),
                        user.getName(),
                        user.getSurname(),
                        user.getRole().name(),
                        user.getLibrary() != null ? user.getLibrary().getId() : null
                ))
                .collect(Collectors.toList());
    }
}