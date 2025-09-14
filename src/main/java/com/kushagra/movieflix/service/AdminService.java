package com.kushagra.movieflix.service;

import com.kushagra.movieflix.dto.CustomResponse;
import com.kushagra.movieflix.entity.Movie;
import com.kushagra.movieflix.entity.MovieDetails;
import com.kushagra.movieflix.entity.User;
import com.kushagra.movieflix.exception.NotFoundException;
import com.kushagra.movieflix.repository.MovieRepository;
import com.kushagra.movieflix.repository.UserRepository;
import com.kushagra.movieflix.utils.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class AdminService {

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MovieService movieService;

    public CustomResponse addMovie(String id) {
        log.info("Entered into addMovie method in AdminService");
        MovieDetails details = (MovieDetails) movieService.getMovieById(id).getResult();

        Movie newMovie = movieRepository.findByExternalId(id).orElseGet(() -> movieRepository.save(
                Movie.builder()
                        .externalId(details.getImdbId())
                        .title(details.getTitle())
                        .year(details.getYear())
                        .type(details.getType())
                        .poster(details.getPoster())
                        .genres(Optional.ofNullable(details.getGenre()).orElse("").isBlank() ? Collections.emptyList() :
                                Arrays.stream(details.getGenre().split(",")).map(String::trim).toList())
                        .rating(CommonUtil.parseRating(details.getImdbRating()))
                        .lastFetchedTimestamp(Instant.now().toEpochMilli())
                        .build()
        ));
        log.info("Updated movie to the database, {}", newMovie);

        return CustomResponse.builder()
                .result(newMovie)
                .status("success")
                .message("Added movie successfully")
                .statusCode(String.valueOf(HttpStatus.CREATED.value())).build();
    }

    public CustomResponse updateMovie(Long id, Movie movie) {
        log.info("Entered into updateMovie method in AdminService");

        Movie existing = movieRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Movie not found"));
        existing.setTitle(movie.getTitle());
        existing.setYear(movie.getYear());
        existing = movieRepository.save(movie);
        log.info("Updated movie to the database");


        return CustomResponse.builder()
                .result(existing)
                .status("success")
                .message("Updated movie successfully")
                .statusCode(String.valueOf(HttpStatus.CREATED.value())).build();
    }

    public CustomResponse deleteMovie(String id) {
        log.info("Entered into deleteMovie method in AdminService");
        Movie movie = movieRepository.findByExternalId(id).orElseThrow(() -> new NotFoundException("No entity with the given id"));

        movieRepository.delete(movie);
        log.info("Deleted data from table successfully");
        return CustomResponse.builder()
                .result(null)
                .status("success")
                .message("Deleted movie successfully")
                .statusCode(String.valueOf(HttpStatus.OK.value())).build();
    }

    public CustomResponse createUser(User user) {
        log.info("Entered into createUser method in AdminService");

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user = userRepository.save(user);

        return CustomResponse.builder()
                .result(user)
                .status("success")
                .message("Added User successfully")
                .statusCode(String.valueOf(HttpStatus.CREATED.value())).build();
    }

    public CustomResponse deleteUser(Long id) {
        log.info("Entered into deleteUser method in AdminService");

        userRepository.deleteById(id);
        return CustomResponse.builder()
                .result(null)
                .status("success")
                .message("Deleted User successfully")
                .statusCode(String.valueOf(HttpStatus.OK.value())).build();
    }

    public CustomResponse getAllUsers() {
        log.info("Entered into getAllUsers method in AdminService");

        List<User> users = userRepository.findAll();

        return CustomResponse.builder()
                .result(users)
                .status("success")
                .message("Fetched All User successfully")
                .statusCode(String.valueOf(HttpStatus.OK.value())).build();
    }
}

