package com.example.library.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "libraries")
public class Library {
    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @OneToMany(mappedBy = "library",  fetch = FetchType.LAZY)
    private List<Copy> stock = new ArrayList<>();

    @OneToMany(mappedBy = "library",  fetch = FetchType.LAZY)
    private List<User> users = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LibraryStatus status;

}
