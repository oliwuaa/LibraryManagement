package com.example.library.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;

    @Column(nullable = false)
    private String password;
    @Column(unique = true, nullable = false)
    private String email;
    @Column
    private String name;
    @Column
    private String surname;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;
    @ManyToOne
    @JoinColumn(name = "library_id")
    private Library library;
}
