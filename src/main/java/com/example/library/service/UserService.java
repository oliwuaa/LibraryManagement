package com.example.library.service;

import com.example.library.model.User;
import com.example.library.model.UserRole;
import com.example.library.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository bookRepository) {
        this.userRepository = bookRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    public User getUserById(Long userId){
        return userRepository.findById(userId).orElseThrow(() -> new IllegalStateException("User with ID " + userId + " does not exist"));
    };

    public List<User> getUsersByRole(UserRole role){
        return userRepository.findUsersByRole(role);
    }

    public List<User> getLibrariansFromLibrary(Long libraryId){
        return userRepository.findLibrarians(UserRole.LIBRARIAN, libraryId);
    }

    public void updateUser(Long userId, User updatedUser){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User with ID " + userId + " does not exist"));

        if (updatedUser.getName() != null && !updatedUser.getName().isEmpty()) {
            user.setName(updatedUser.getName());
        }

        if (updatedUser.getEmail() != null && !updatedUser.getEmail().isEmpty()) {
            if (userRepository.findByEmail(updatedUser.getEmail()).isPresent()) {
                throw new IllegalStateException("Email already taken");
            }
            user.setEmail(updatedUser.getEmail());
        }

        userRepository.save(user);
    }

    public void deleteUser(Long userId){
        if (!userRepository.existsById(userId)) {
            throw new IllegalStateException("User not found");
        }
        userRepository.deleteById(userId);
    }
}
