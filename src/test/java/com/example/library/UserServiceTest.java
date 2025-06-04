package com.example.library;

import com.example.library.dto.UserRegistrationDTO;
import com.example.library.exception.BadRequestException;
import com.example.library.exception.NotFoundException;
import com.example.library.model.User;
import com.example.library.model.UserRole;
import com.example.library.repository.LibraryRepository;
import com.example.library.repository.LoanRepository;
import com.example.library.repository.ReservationRepository;
import com.example.library.repository.UserRepository;
import com.example.library.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LibraryRepository libraryRepository;

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void getCurrentUser_throwsException_whenUserNotFound() {
        when(authentication.getName()).thenReturn("notfound@example.com");
        when(userRepository.findByEmailAndActiveTrue("notfound@example.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getCurrentUser())
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void getUserById_throws_whenUserDoesNotExist() {
        when(userRepository.findById(123L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(123L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User with ID 123 does not exist");
    }


    @Test
    void addUser_savesUser_whenEmailIsFree() {
        UserRegistrationDTO dto = new UserRegistrationDTO("test@example.com", "pass123", "Anna", "Nowak");
        when(userRepository.findByEmailAndActiveTrue(dto.email())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(dto.password())).thenReturn("encodedPassword");

        userService.addUser(dto);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(savedUser.getPassword()).isEqualTo("encodedPassword");
        assertThat(savedUser.getName()).isEqualTo("Anna");
        assertThat(savedUser.getSurname()).isEqualTo("Nowak");
        assertThat(savedUser.getRole()).isEqualTo(UserRole.USER);
    }

    @Test
    void addUser_throws_whenEmailTaken() {
        UserRegistrationDTO dto = new UserRegistrationDTO("taken@example.com", "pass", "Jan", "Kowalski");
        when(userRepository.findByEmailAndActiveTrue("taken@example.com"))
                .thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> userService.addUser(dto))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Email already taken");
    }

    @Test
    void deleteUser_throws_whenUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void deleteUser_setsActiveFalse_whenUserExists() {
        User user = new User();
        user.setId(1L);
        user.setActive(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deleteUser(1L);

        assertThat(user.isActive()).isFalse();
        verify(userRepository).save(user);
    }

    @Test
    void changeRole_throws_whenUserNotFound() {
        when(userRepository.findById(123L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.changeRole(123L, UserRole.LIBRARIAN, 1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User with ID 123 does not exist");
    }

    @Test
    void changeRole_throws_whenLibraryIdMissingForLibrarian() {
        User user = new User();
        user.setId(1L);
        user.setRole(UserRole.USER);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.changeRole(1L, UserRole.LIBRARIAN, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Library Id needed");
    }

    @Test
    void getLibrariansFromLibrary_throws_whenLibraryNotFound() {
        when(libraryRepository.existsById(100L)).thenReturn(false);

        assertThatThrownBy(() -> userService.getLibrariansFromLibrary(100L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Library with ID 100 does not exist");
    }

}