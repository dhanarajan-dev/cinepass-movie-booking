package com.moviebooking.service;

import com.moviebooking.model.Movie;
import com.moviebooking.repository.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MovieService {

    @Autowired
    private MovieRepository movieRepository;

    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }

    public List<Movie> getRecommendedMovies() {
        return movieRepository.findByIsRecommendedTrue();
    }

    public Optional<Movie> getMovieById(Long id) {
        return movieRepository.findById(id);
    }

    public List<Movie> searchMovies(String query) {
        if (query == null || query.trim().isEmpty()) {
            return movieRepository.findAll();
        }
        return movieRepository.findByTitleContainingIgnoreCaseOrGenreContainingIgnoreCase(query.trim(), query.trim());
    }
}
