package com.moviebooking.controller;

import com.moviebooking.dto.ApiResponse;
import com.moviebooking.model.Show;
import com.moviebooking.service.ShowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/shows")
public class ShowController {

    @Autowired
    private ShowService showService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Show>>> getShows(
            @RequestParam Long movieId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        LocalDate queryDate = (date != null) ? date : LocalDate.now();
        List<Show> shows = showService.getShowsByMovieAndDate(movieId, queryDate);
        return ResponseEntity.ok(ApiResponse.success("Shows retrieved successfully", shows));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Show>> getShowById(@PathVariable Long id) {
        return showService.getShowById(id)
                .map(show -> ResponseEntity.ok(ApiResponse.success("Show details retrieved", show)))
                .orElseGet(() -> ResponseEntity.status(404).body(ApiResponse.error("Show not found with ID: " + id)));
    }

    @GetMapping("/{id}/booked-seats")
    public ResponseEntity<ApiResponse<List<String>>> getBookedSeats(@PathVariable Long id) {
        List<String> bookedSeats = showService.getBookedSeatsForShow(id);
        return ResponseEntity.ok(ApiResponse.success("Booked seats retrieved", bookedSeats));
    }
}
