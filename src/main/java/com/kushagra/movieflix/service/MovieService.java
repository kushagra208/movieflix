package com.kushagra.movieflix.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kushagra.movieflix.dto.CustomResponse;
import com.kushagra.movieflix.dto.MovieCategoryDto;
import com.kushagra.movieflix.dto.MovieSearchResponse;
import com.kushagra.movieflix.dto.MovieSummaryDto;
import com.kushagra.movieflix.entity.Movie;
import com.kushagra.movieflix.entity.MovieDetails;
import com.kushagra.movieflix.exception.NotFoundException;
import com.kushagra.movieflix.repository.MovieDetailsRepository;
import com.kushagra.movieflix.repository.MovieRepository;
import com.kushagra.movieflix.utils.CommonUtil;
import com.kushagra.movieflix.utils.RestClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MovieService {

    @Autowired
    private MovieRepository movieCacheRepository;

    @Autowired
    private MovieDetailsRepository movieDetailsRepository;

    @Value("${omdb.api.key}")
    private String apiKey;

    @Autowired
    private ObjectMapper objectMapper;

    private final String BASE_URL = "http://www.omdbapi.com/";

    // categories used to seed home page if cache is empty
    private final Map<String, List<String>> categoryKeywords = Map.of(
            "Action", List.of("Avengers", "Batman", "John Wick"),
            "Comedy", List.of("Friends", "The Hangover", "The Office"),
            "Sci-Fi", List.of("Inception", "Interstellar", "Matrix"),
            "Fantasy", List.of("Harry Potter", "Lord of the Rings", "Hobbit")
    );

    // ---------------- 1) HOMEPAGE (no search) - paginated from cache ----------------
    /**
     * Return paginated movies for homepage when there is no search.
     * If cache is empty for a category it will seed a page of results from OMDb (synchronously),
     * but only store summaries (not full details) to keep seeding fast.
     */
    public CustomResponse getHomePageMovies(int page, int size, String sort) {
        log.info("Entered getHomePageMovies(page={}, size={})", page, size);

        // Query DB for all cached movies (you can change to per-category logic if needed)
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, CommonUtil.isNullOrEmpty(sort) || sort.equalsIgnoreCase("year") ? "year": "rating"));
        Page<Movie> moviesPage = movieCacheRepository.findAll(pageable);

        // If DB empty, try to seed from OMDb categories (only once-ish)
        if (moviesPage.isEmpty()) {
            seedCacheFromCategories(size); // fills cache with summaries, non-blocking minimal hits
            moviesPage = movieCacheRepository.findAll(pageable);
        }

        return buildPaginatedResponseFromPage(moviesPage, page);
    }

    // small seeding - only saves summaries for a few keywords per category to populate homepage quickly
    private void seedCacheFromCategories(int perCategoryLimit) {
        log.info("Seeding cache from OMDb categories");
        for (Map.Entry<String, List<String>> entry : categoryKeywords.entrySet()) {
            for (String kw : entry.getValue()) {
                try {
                    String url = BASE_URL + "?apikey=" + apiKey + "&s=" + urlEncode(kw) + "&page=1";
                    String resp = RestClientUtil.restClient(url, null, null, "GET");
                    if (resp == null || resp.isBlank()) continue;
                    MovieSearchResponse searchResponse = mapResponseToDTO(resp);
                    if (searchResponse == null || searchResponse.getSearch() == null) continue;

                    List<MovieSummaryDto> results = searchResponse.getSearch().stream()
                            .limit(perCategoryLimit)
                            .collect(Collectors.toList());
                    fetchDetailsAsync(results.stream().map(MovieSummaryDto::getImdbId).collect(Collectors.toList()));

                    // Save summaries (no full details)
                    for (MovieSummaryDto s : results) {
                        movieCacheRepository.findByExternalId(s.getImdbId())
                                .orElseGet(() -> {
                                    Movie m = Movie.builder()
                                            .externalId(s.getImdbId())
                                            .title(s.getTitle())
                                            .year(s.getYear())
                                            .type(s.getType())
                                            .poster(s.getPoster())
                                            .lastFetchedTimestamp(Instant.now().toEpochMilli())
                                            .build();
                                    return movieCacheRepository.save(m);
                                });
                    }
                } catch (Exception e) {
                    log.warn("Seeding failed for keyword {} : {}", kw, e.getMessage());
                }
            }
        }
    }

    // ---------------- 2) SEARCH (title present) - call OMDb search endpoint and return OMDb pagination ----------------
    /**
     * Search by title: always call OMDb search API for the requested page.
     * We store only summaries quickly in cache and trigger background detail fetch (async) without blocking.
     *
     * Returns: result map with keys:
     *   data -> List<Movie> (summaries from cache corresponding to this OMDb page)
     *   pageNumber, size, totalPages, total  -> derived from OMDb totalResults and page/size
     */
    public CustomResponse searchMovies(String search, String sort, String filter, int page, int size) {
        log.info("Entered searchMovies(search='{}', page={}, size={}, sort={}, filter={})", search, page, size, sort, filter);

        if (search == null || search.isBlank()) {
            // no search => behave like homepage
            return getHomePageMovies(page, size, sort);
        }

        // call OMDb search endpoint for the requested page (OMDb page is 1-indexed and returns 10 items per page)
        int omdbPage = page + 1;
        String url = BASE_URL + "?apikey=" + apiKey + "&s=" + urlEncode(search) + "&page=" + omdbPage;
        String response = RestClientUtil.restClient(url, null, null, "GET");

        if (response == null || response.isBlank()) {
            return CustomResponse.builder()
                    .status("success")
                    .statusCode(String.valueOf(HttpStatus.OK.value()))
                    .message("No movie found")
                    .result(Map.of(
                            "data", Collections.emptyList(),
                            "pageNumber", page,
                            "size", 0,
                            "totalPages", 0,
                            "total", 0
                    ))
                    .build();
        }

        MovieSearchResponse searchResponse = mapResponseToDTO(response);
        List<MovieSummaryDto> summaries = Optional.ofNullable(searchResponse.getSearch()).orElse(Collections.emptyList());

        // Save summaries into cache quickly (no blocking detail fetch)
        for (MovieSummaryDto s : summaries) {
            movieCacheRepository.findByExternalId(s.getImdbId())
                    .orElseGet(() -> movieCacheRepository.save(Movie.builder()
                            .externalId(s.getImdbId())
                            .title(s.getTitle())
                            .year(s.getYear())
                            .type(s.getType())
                            .poster(s.getPoster())
                            .lastFetchedTimestamp(Instant.now().toEpochMilli())
                            .build()));
        }

        // Asynchronously fetch & save full details for these summaries (does not block response)
        fetchDetailsAsync(summaries.stream().map(MovieSummaryDto::getImdbId).collect(Collectors.toList()));

        // Build response using OMDb's totalResults so pagination is consistent with OMDb
        int totalResults = 0;
        try {
            totalResults = (searchResponse.getTotalResults() == null) ? 0 : Integer.parseInt(searchResponse.getTotalResults());
        } catch (Exception ignored) {}

        int returnedSize = summaries.size();
        int pageSize = size > 0 ? size : returnedSize; // frontend controlled; OMDb default is 10 per OMDb page
        int totalPages = (pageSize == 0) ? 0 : (int) Math.ceil((double) totalResults / pageSize);

        // Convert saved cache entries (summaries) to Movie objects for response (preserve ordering)
        List<Movie> resultList = summaries.stream()
                .map(s -> movieCacheRepository.findByExternalId(s.getImdbId())
                        .orElse(buildMovieFromSummary(s)))
                .sorted((m1, m2) -> {
                    if (sort == null || sort.isBlank()) {
                        return 0; // no sorting
                    }
                    switch (sort.toLowerCase()) {
                        case "year":
                            return Optional.ofNullable(m2.getYear()).orElse("")
                                    .compareTo(Optional.ofNullable(m1.getYear()).orElse(""));
                        case "rating":
                            return Optional.ofNullable(m2.getRating()).orElse((double) 0)
                                    .compareTo(Optional.ofNullable(m1.getRating()).orElse((double) 0));
                        default:
                            return 0; // unknown sort → no sorting
                    }
                })
                .collect(Collectors.toList());


        Map<String, Object> result = new HashMap<>();
        result.put("data", resultList);
        result.put("size", resultList.size());
        result.put("pageNumber", page);
        result.put("totalPages", totalPages);
        result.put("total", totalResults);

        return CustomResponse.builder()
                .result(result)
                .status("success")
                .statusCode(String.valueOf(HttpStatus.OK.value()))
                .message("Search results (from OMDb) returned")
                .build();
    }

    // build a Movie object from summary without persisting
    private Movie buildMovieFromSummary(MovieSummaryDto s) {
        return Movie.builder()
                .externalId(s.getImdbId())
                .title(s.getTitle())
                .year(s.getYear())
                .type(s.getType())
                .poster(s.getPoster())
                .build();
    }

    // ---------------- 3) MOVIE DETAILS (single movie) - synchronous on-demand fetch if not cached ----------------
    /**
     * Fetch details for a single movie id. If details exist in DB return directly,
     * otherwise call OMDb details endpoint, save both details and update cache (fast).
     */
    public CustomResponse getMovieById(String imdbId) {
        log.info("Entered getMovieById for {}", imdbId);

        Optional<MovieDetails> cached = movieDetailsRepository.findByImdbId(imdbId);
        if (cached.isPresent()) {
            return CustomResponse.builder()
                    .result(cached.get())
                    .status("success")
                    .statusCode(String.valueOf(HttpStatus.OK.value()))
                    .message("Movie details returned from cache")
                    .build();
        }

        // Not in details DB -> fetch from OMDb
        String url = BASE_URL + "?apikey=" + apiKey + "&i=" + imdbId + "&plot=full";
        String response = RestClientUtil.restClient(url, null, null, "GET");
        if (response == null || response.isBlank()) {
            throw new NotFoundException("Movie not found");
        }

        try {
            MovieDetails details = objectMapper.readValue(response, MovieDetails.class);
            movieDetailsRepository.save(details);

            // ensure summary cache exists / update it
            movieCacheRepository.findByExternalId(imdbId).orElseGet(() -> movieCacheRepository.save(
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

            return CustomResponse.builder()
                    .result(details)
                    .status("success")
                    .statusCode(String.valueOf(HttpStatus.OK.value()))
                    .message("Movie details fetched from OMDb")
                    .build();

        } catch (Exception e) {
            log.error("Error mapping OMDb response to MovieDetails", e);
            throw new RuntimeException("Internal error while parsing movie details");
        }
    }

    // ---------------- Async: fetch details in background for a list of imdbIds ----------------
    // Does not block caller — ideal to warm cache after search
    @Async
    public void fetchDetailsAsync(List<String> imdbIds) {
        if (imdbIds == null || imdbIds.isEmpty()) return;
        for (String id : imdbIds) {
            try {
                // skip if already have details
                if (movieDetailsRepository.findByImdbId(id).isPresent()) continue;
                String url = BASE_URL + "?apikey=" + apiKey + "&i=" + id + "&plot=short";
                String response = RestClientUtil.restClient(url, null, null, "GET");
                if (response == null || response.isBlank()) continue;
                MovieDetails details = objectMapper.readValue(response, MovieDetails.class);
                movieDetailsRepository.save(details);

                // update summary cache genres/rating if available
                movieCacheRepository.findByExternalId(id).ifPresent(m -> {
                    if ((m.getGenres() == null || m.getGenres().isEmpty()) && details.getGenre() != null) {
                        m.setGenres(Arrays.stream(details.getGenre().split(",")).map(String::trim).toList());
                    }
                    if (m.getRating() == null && details.getImdbRating() != null && !details.getImdbRating().equalsIgnoreCase("N/A")) {
                        m.setRating(CommonUtil.parseRating(details.getImdbRating()));
                    }
                    m.setLastFetchedTimestamp(Instant.now().toEpochMilli());
                    movieCacheRepository.save(m);
                });
            } catch (Exception e) {
                log.warn("Async detail fetch failed for {} : {}", id, e.getMessage());
            }
        }
    }

    // ---------------- Helpers ----------------
    private CustomResponse buildPaginatedResponseFromPage(Page<Movie> pageObj, int page) {
        Map<String, Object> result = new HashMap<>();
        result.put("data", pageObj.getContent());
        result.put("size", pageObj.getSize());
        result.put("pageNumber", page);
        result.put("totalPages", pageObj.getTotalPages());
        result.put("total", pageObj.getTotalElements());

        return CustomResponse.builder()
                .result(result)
                .status("success")
                .statusCode(String.valueOf(HttpStatus.OK.value()))
                .message("Movies fetched successfully (page " + page + ")")
                .build();
    }

    private String urlEncode(String s) {
        try { return java.net.URLEncoder.encode(s, java.nio.charset.StandardCharsets.UTF_8); }
        catch (Exception e) { return s; }
    }

    // Map OMDb search response -> DTO
    private MovieSearchResponse mapResponseToDTO(String response) {
        try {
            return objectMapper.readValue(response, MovieSearchResponse.class);
        } catch (Exception e) {
            log.error("Error mapping OMDb search response", e);
            throw new RuntimeException("Internal Server Error");
        }
    }
}
