package com.kushagra.movieflix.entity;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "movie_details")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true) // ignore fields you don’t care about
public class MovieDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonProperty("imdbID")   // OMDb sends imdbID
    private String imdbId;

    @JsonProperty("Title")
    private String title;

    @JsonProperty("Year")
    private String year;

    @JsonProperty("Rated")
    private String rated;

    @JsonProperty("Released")
    private String released;

    @JsonProperty("Runtime")
    private String runtime;

    @JsonProperty("Genre")
    private String genre;

    @JsonProperty("Director")
    private String director;

    @JsonProperty("Writer")
    private String writer;

    @JsonProperty("Actors")
    private String actors;

    @JsonProperty("Plot")
    @Column(length = 2000)
    private String plot;

    @JsonProperty("Language")
    private String language;

    @JsonProperty("Country")
    private String country;

    @JsonProperty("Awards")
    private String awards;

    @JsonProperty("Poster")
    private String poster;

    @JsonProperty("Metascore")
    private String metascore;

    @JsonProperty("imdbRating")
    @JsonAlias("IMDbRating") // in case API changes casing
    private String imdbRating; // Keep as String → safer for "N/A"

    @JsonProperty("imdbVotes")
    private String imdbVotes;

    @JsonProperty("Type")
    private String type;

    @JsonProperty("totalSeasons")
    private String totalSeasons;

    @JsonProperty("Ratings") // Maps the JSON array
    @ElementCollection
    @CollectionTable(name = "movie_ratings", joinColumns = @JoinColumn(name = "movie_id"))
    private List<Rating> ratings;

    @Embeddable
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Rating {
        @JsonProperty("Source")
        private String source;

        @JsonProperty("Value")
        private String value;
    }
}

