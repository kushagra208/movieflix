package com.kushagra.movieflix.service;

import com.kushagra.movieflix.dto.CustomResponse;
import com.kushagra.movieflix.entity.Movie;
import com.kushagra.movieflix.entity.MovieDetails;
import com.kushagra.movieflix.repository.MovieDetailsRepository;
import com.kushagra.movieflix.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class StatsService {

    private final MovieDetailsRepository movieDetailsRepository;

    public CustomResponse getMovieStats() {
        log.info("Generating movie statistics...");

        List<MovieDetails> movies = movieDetailsRepository.findAll();
        if (movies.isEmpty()) {
            return CustomResponse.builder()
                    .status("success")
                    .statusCode(String.valueOf(HttpStatus.OK.value()))
                    .message("No movies available for stats")
                    .result(Collections.emptyMap())
                    .build();
        }

        Map<String, Object> stats = new HashMap<>();

        // 1. Genre counts
        Map<String, Long> genreCounts = movies.stream()
                .filter(m -> m.getGenre() != null)
                .flatMap(m -> Arrays.stream(m.getGenre().split(",")).sequential())
                .collect(Collectors.groupingBy(String::trim, Collectors.counting()));

        stats.put("genreCounts", genreCounts);

        // 2. Average rating
        Double avgRating = movies.stream()
                .filter(m -> m.getImdbRating() != null && !m.getImdbRating().equalsIgnoreCase("N/A"))
                .mapToDouble(m -> Double.parseDouble(m.getImdbRating()))
                .average()
                .orElse(0.0);

        stats.put("averageRating", avgRating);

        // 3. Average runtime per year
        Map<String, Double> avgRuntimeByYear = movies.stream()
                .filter(m -> m.getYear() != null && m.getRuntime() != null && Long.parseLong(m.getRuntime().split(" ")[0]) > 0)
                .collect(Collectors.groupingBy(
                        MovieDetails::getYear,
                        Collectors.averagingInt(m -> Integer.parseInt(m.getRuntime().split(" ")[0]))
                ));

        stats.put("avgRuntimeByYear", avgRuntimeByYear);

        return CustomResponse.builder()
                .status("success")
                .statusCode(String.valueOf(HttpStatus.OK.value()))
                .message("Movie statistics fetched successfully")
                .result(stats)
                .build();
    }
}
