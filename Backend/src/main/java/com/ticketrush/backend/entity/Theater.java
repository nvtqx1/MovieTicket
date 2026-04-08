package com.ticketrush.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "theaters")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Theater {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false)
    private String location;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

}
