package com.moviebooking.controller;

import com.moviebooking.dto.ApiResponse;
import com.moviebooking.model.Movie;
import com.moviebooking.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
public class MovieController {

    @Autowired
    private MovieService movieService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Movie>>> getAllMovies(@RequestParam(value = "search", required = false) String search) {
        List<Movie> movies = movieService.searchMovies(search);
        return ResponseEntity.ok(ApiResponse.success("Movies retrieved successfully", movies));
    }

    @GetMapping("/recommended")
    public ResponseEntity<ApiResponse<List<Movie>>> getRecommendedMovies() {
        List<Movie> movies = movieService.getRecommendedMovies();
        return ResponseEntity.ok(ApiResponse.success("Recommended movies retrieved successfully", movies));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Movie>> getMovieById(@PathVariable Long id) {
        return movieService.getMovieById(id)
                .map(movie -> ResponseEntity.ok(ApiResponse.success("Movie details retrieved successfully", movie)))
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.error("Movie not found with ID: " + id)));
    }
}
