package com.kushagra.movieflix.repository;

import com.kushagra.movieflix.entity.MovieDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MovieDetailsRepository extends JpaRepository<MovieDetails, Long> {
    Optional<MovieDetails> findByImdbId(String imdbId);
}
