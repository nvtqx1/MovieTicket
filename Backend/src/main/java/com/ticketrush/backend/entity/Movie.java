package com.ticketrush.backend.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity biểu diễn phim trong hệ thống.
 */
@Entity
@Table(name = "movies")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "release_year")
    private Integer releaseYear;

    @Column(length = 100)
    private String genre;

    @Column(name = "poster_image_url")
    private String posterImageUrl;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

}
