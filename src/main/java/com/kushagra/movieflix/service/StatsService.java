package com.kushagra.movieflix.service;

import com.kushagra.movieflix.dto.CustomResponse;
import com.kushagra.movieflix.entity.MovieDetails;
import com.kushagra.movieflix.repository.MovieDetailsRepository;
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

    public CustomResponse getStats() {
        log.info("Fetching stats for dashboard (MovieDetails)");

        List<MovieDetails> allMovies = movieDetailsRepository.findAll();

        // --- 1. Genre Distribution ---
        Map<String, Long> genreDistribution = allMovies.stream()
                .filter(m -> m.getGenre() != null)
                .flatMap(m -> Arrays.stream(m.getGenre().split(",")))
                .map(String::trim)
                .filter(g -> !g.isBlank())
                .collect(Collectors.groupingBy(g -> g, Collectors.counting()));

        // --- 2. Avg Rating by Genre ---
        Map<String, Double> avgRatingByGenre = allMovies.stream()
                .filter(m -> m.getGenre() != null && m.getImdbRating() != null)
                .flatMap(m -> Arrays.stream(m.getGenre().split(","))
                        .map(String::trim)
                        .filter(g -> !g.isBlank())
                        .map(g -> new AbstractMap.SimpleEntry<>(g, parseRating(m.getImdbRating()))))
                .filter(e -> e.getValue() != null)
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.averagingDouble(Map.Entry::getValue)
                ));

        // --- 3. Avg Runtime by Year ---
        Map<String, Double> avgRuntimeByYear = allMovies.stream()
                .filter(m -> m.getYear() != null && m.getRuntime() != null)
                .collect(Collectors.groupingBy(
                        MovieDetails::getYear,
                        Collectors.averagingInt(m -> parseRuntime(m.getRuntime()))
                ));

        Map<String, Object> result = new HashMap<>();
        result.put("genreDistribution", genreDistribution);
        result.put("avgRatingByGenre", avgRatingByGenre);
        result.put("avgRuntimeByYear", avgRuntimeByYear);

        return CustomResponse.builder()
                .status("success")
                .statusCode(String.valueOf(HttpStatus.OK.value()))
                .message("Stats fetched successfully")
                .result(result)
                .build();
    }

    private Double parseRating(String rating) {
        try {
            return rating != null && !rating.equalsIgnoreCase("N/A")
                    ? Double.parseDouble(rating)
                    : 5;
        } catch (Exception e) {
            log.error("Error while parsing data to double: {}", e.getMessage());
            throw new RuntimeException("Internal Server Error");
        }
    }

    private Integer parseRuntime(String runtime) {
        try {
            // OMDb usually returns like "142 min"
            if (runtime != null && runtime.contains("min")) {
                return Integer.parseInt(runtime.replace("min", "").trim());
            }
            return 50;
        } catch (Exception e) {
            log.error("Error while parsing data to double: {}", e.getMessage());
            return 50;
        }
    }
}
