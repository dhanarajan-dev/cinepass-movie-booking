package com.moviebooking.controller;

import com.moviebooking.dto.ApiResponse;
import com.moviebooking.dto.BookingRequest;
import com.moviebooking.model.Booking;
import com.moviebooking.model.User;
import com.moviebooking.service.BookingService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping
    public ResponseEntity<ApiResponse<Booking>> createBooking(@Valid @RequestBody BookingRequest request, HttpSession session) {
        User currentUser = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("You must be logged in to book movie tickets."));
        }

        try {
            Booking booking = bookingService.createBooking(currentUser, request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Booking confirmed successfully! An e-ticket has been sent to your email.", booking));
        } catch (IllegalStateException e) {
            // Requirement 9: Concurrency conflict (seat already booked)
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An unexpected error occurred during booking. Please try again."));
        }
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<Booking>>> getMyBookings(HttpSession session) {
        User currentUser = (User) session.getAttribute(AuthController.SESSION_USER_KEY);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Please log in to view your bookings."));
        }

        try {
            List<Booking> bookings = bookingService.getUserBookings(currentUser.getId());
            return ResponseEntity.ok(ApiResponse.success("User bookings retrieved", bookings));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to load booking history: " + e.getMessage()));
        }
    }

    @GetMapping("/{bookingNumber}")
    public ResponseEntity<ApiResponse<Booking>> getBookingByNumber(@PathVariable String bookingNumber, HttpSession session) {
        return bookingService.getBookingByNumber(bookingNumber)
                .map(b -> ResponseEntity.ok(ApiResponse.success("Booking details retrieved", b)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Booking not found.")));
    }
}
