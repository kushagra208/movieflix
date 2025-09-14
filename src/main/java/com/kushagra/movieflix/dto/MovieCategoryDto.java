package com.kushagra.movieflix.dto;


import com.kushagra.movieflix.entity.Movie;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MovieCategoryDto {
    private String category;
    private List<Movie> movies;
}