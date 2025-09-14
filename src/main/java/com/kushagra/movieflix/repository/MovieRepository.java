package com.kushagra.movieflix.repository;

import com.kushagra.movieflix.entity.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MovieRepository extends JpaRepository<Movie, Long> {
    Optional<Movie> findByExternalId(String externalId);
    List<Movie> findByTitleContainingIgnoreCase(String title);
    Page<Movie> findByTitleContainingIgnoreCaseAndGenresIn(
            String title, List<String> genres, Pageable pageable);
    Page<Movie> findByTitleContainingIgnoreCase(String title, Pageable pageable);

}
