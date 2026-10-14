package com.moviebooking.service;

import com.moviebooking.dto.BookingRequest;
import com.moviebooking.model.Booking;
import com.moviebooking.model.BookingSeat;
import com.moviebooking.model.Show;
import com.moviebooking.model.User;
import com.moviebooking.repository.BookingRepository;
import com.moviebooking.repository.BookingSeatRepository;
import com.moviebooking.repository.ShowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BookingSeatRepository bookingSeatRepository;

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private EmailService emailService;

    /**
     * Creates a new movie ticket booking with full concurrency protection and seat availability management.
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public synchronized Booking createBooking(User user, BookingRequest request) {
        if (!request.isTermsAccepted()) {
            throw new IllegalArgumentException("You must accept the terms and conditions to proceed with booking.");
        }

        if (request.getSelectedSeats() == null || request.getSelectedSeats().isEmpty()) {
            throw new IllegalArgumentException("Please select at least one seat.");
        }

        Show show = showRepository.findById(request.getShowId())
                .orElseThrow(() -> new IllegalArgumentException("Selected showtime does not exist."));

        // Clean & uppercase seat numbers
        List<String> seatsToBook = request.getSelectedSeats().stream()
                .map(String::trim)
                .map(String::toUpperCase)
                .distinct()
                .toList();

        // Requirement 9: Check seat availability to prevent double-booking
        List<String> conflicts = bookingSeatRepository.findConflictingSeats(show.getId(), seatsToBook);
        if (!conflicts.isEmpty()) {
            throw new IllegalStateException("Seat(s) " + String.join(", ", conflicts) + " have already been booked by another user. Please choose different seats.");
        }

        // Calculate total amount based on seat categories (Requirement 5)
        // Premium: Rows A, B, C, D
        // Elite: Rows E, F, G, H
        double totalAmount = 0.0;
        List<BookingSeat> bookingSeatsList = new ArrayList<>();

        for (String seatNum : seatsToBook) {
            String row = seatNum.substring(0, 1).toUpperCase();
            String category;
            double price;

            if (row.compareTo("A") >= 0 && row.compareTo("D") <= 0) {
                category = "PREMIUM";
                price = show.getPremiumPrice();
            } else {
                category = "ELITE";
                price = show.getElitePrice();
            }

            totalAmount += price;
        }

        // Generate unique booking number and transaction ID
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        String bookingNumber = "CP-" + datePart + "-" + randomPart;
        String transactionId = "TXN-" + System.currentTimeMillis() + "-" + randomPart;

        String seatsDisplay = String.join(", ", seatsToBook);

        // Requirement 7: User's registered email and mobile are permanently tied to this booking
        Booking booking = new Booking(
                bookingNumber,
                user,
                show,
                totalAmount,
                seatsToBook.size(),
                seatsDisplay,
                request.getPaymentMethod(),
                "SUCCESS",
                transactionId,
                user.getEmail(),
                user.getPhone()
        );

        Booking savedBooking = bookingRepository.save(booking);

        // Save seats
        for (String seatNum : seatsToBook) {
            String row = seatNum.substring(0, 1).toUpperCase();
            String category = (row.compareTo("A") >= 0 && row.compareTo("D") <= 0) ? "PREMIUM" : "ELITE";
            double price = category.equals("PREMIUM") ? show.getPremiumPrice() : show.getElitePrice();

            BookingSeat bookingSeat = new BookingSeat(show, savedBooking, seatNum, category, price);
            bookingSeatRepository.save(bookingSeat);
            bookingSeatsList.add(bookingSeat);
        }

        savedBooking.setSeats(bookingSeatsList);

        // Requirement 10: Send E-Ticket confirmation email to user's registered email address
        try {
            emailService.sendBookingConfirmationTicket(savedBooking);
        } catch (Exception e) {
            // Non-blocking for UI, error logged in service
        }

        return savedBooking;
    }

    @Transactional(readOnly = true)
    public List<Booking> getUserBookings(Long userId) {
        return bookingRepository.findByUserIdOrderByBookingTimeDesc(userId);
    }

    @Transactional(readOnly = true)
    public Optional<Booking> getBookingByNumber(String bookingNumber) {
        return bookingRepository.findByBookingNumber(bookingNumber);
    }
}
