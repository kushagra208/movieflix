package com.kushagra.movieflix.controller;

import com.kushagra.movieflix.dto.CustomResponse;
import com.kushagra.movieflix.service.MovieService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/movies")
@Slf4j
public class MovieController {

    @Autowired
    private MovieService movieService;

    @GetMapping("/all")
    public ResponseEntity<CustomResponse> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("Entered into getAll method in MoviesController");
        return ResponseEntity.ok(movieService.getHomePageMovies(page, size, "year"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomResponse> getMovie(@PathVariable String id) {
        log.info("Entered into getMovie method in MoviesController");
        return ResponseEntity.ok(movieService.getMovieById(id));
    }

    @GetMapping()
    public ResponseEntity<CustomResponse> searchMovies(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String filter) {
        log.info("Entered into searchMovies method in MoviesController");

        return ResponseEntity.ok(movieService.searchMovies(search, sort, filter, page, size));
    }
}