package com.example.library.service;

import com.example.library.model.*;
import com.example.library.repository.CopyRepository;
import com.example.library.repository.LibraryRepository;
import com.example.library.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class LibraryService {
    @Autowired
    private final LibraryRepository libraryRepository;
    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final CopyRepository copyRepository;


    public List<Library> getAllLibraries() {
        return libraryRepository.findAll();
    }

    public List<Library> getActiveLibraries() {
        return libraryRepository.findByStatusIn(List.of(LibraryStatus.ACTIVE, LibraryStatus.CLOSED));
    }

    public void addLibrary(Library newLibrary) throws IllegalAccessException {
        if (libraryRepository.findLibraryByAddress(newLibrary.getAddress()).isPresent())
            throw new IllegalAccessException("This library has already exist");
        libraryRepository.save(newLibrary);

    }

    public List<Copy> getStock(Long libraryId) {
        Library library = libraryRepository.findById(libraryId)
                .orElseThrow(() -> new RuntimeException("Library not found"));
        return copyRepository.findByLibraryId(libraryId);
    }

    public List<User> getLibrariansForLibrary(Long libraryId) {
        Library library = libraryRepository.findById(libraryId)
                .orElseThrow(() -> new IllegalStateException("Library with id " + libraryId + " not found"));

        return userRepository.findLibrarians(UserRole.LIBRARIAN, libraryId);
    }

    public void deleteLibrary(Long libraryId) throws IllegalAccessException {

        Library library = libraryRepository.findById(libraryId)
                .orElseThrow(() -> new RuntimeException("Library with ID " + libraryId + " doesn't exist."));

        List<Copy> copies = library.getStock();
        for (Copy copy : copies) {
            copy.setStatus(CopyStatus.REMOVED);
            copyRepository.save(copy);
        }

        List<User> librarians = library.getUsers();
        for (User user : librarians) {
            if (user.getRole() == UserRole.LIBRARIAN) {
                user.setRole(UserRole.USER);
            }
            user.setLibrary(null);
            userRepository.save(user);
        }
        library.setStatus(LibraryStatus.CLOSED);
        libraryRepository.save(library);
    }

    @Transactional
    public void updateLibrary(Long libraryId, String name, String address) throws IllegalAccessException {
        Library changedLibrary = libraryRepository.findById(libraryId).orElseThrow(() -> new IllegalStateException("Library with ID " + libraryId + " does not exist"));

        if (name != null && name.length() > 0 && !Objects.equals(changedLibrary.getName(), name)) {
            changedLibrary.setName(name);
        }

        if (address != null && address.length() > 0 && !Objects.equals(changedLibrary.getAddress(), address)) {
            changedLibrary.setAddress(address);
        }
    }

}
