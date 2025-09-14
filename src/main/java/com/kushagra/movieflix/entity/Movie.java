package com.kushagra.movieflix.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "movies_cache")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String externalId;   // unique identifier from OMDb
    private String title;
    private String year;
    private String type;     // movie / series / episode
    private String poster;
    @ElementCollection
    @CollectionTable(name = "movie_genres", joinColumns = @JoinColumn(name = "movie_id"))
    @Column(name = "genre")
    private List<String> genres;
    private Integer rating;

    // Helps with refreshing cache if stale
    private Long lastFetchedTimestamp;
}
