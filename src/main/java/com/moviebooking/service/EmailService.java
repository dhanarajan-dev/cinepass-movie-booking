
package com.moviebooking.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moviebooking.model.Booking;
import com.moviebooking.model.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class EmailService {

    private static final Logger logger =
            LoggerFactory.getLogger(EmailService.class);

    private static final String BREVO_API_URL =
            "https://api.brevo.com/v3/smtp/email";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.mail.enabled:true}")
    private boolean mailEnabled;

    @Value("${BREVO_API_KEY:}")
    private String brevoApiKey;

    @Value("${BREVO_FROM_EMAIL:}")
    private String brevoFromEmail;

    // --------------------------------------------------
    // 1. LOGIN SUCCESS NOTIFICATION
    // --------------------------------------------------

    public void sendLoginNotification(User user) {

        if (user == null || user.getEmail() == null
                || user.getEmail().isBlank()) {
            logger.warn("Cannot send login email: user/email is empty.");
            return;
        }

        final String recipientEmail =
                user.getEmail().trim().toLowerCase();

        final String userName =
                (user.getName() != null && !user.getName().isBlank())
                        ? user.getName().trim()
                        : "Valued User";

        final String loginTime = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern(
                        "dd MMMM yyyy, hh:mm:ss a", Locale.ENGLISH));

        CompletableFuture.runAsync(() -> {
            try {
                String subject =
                        "Login Success Notification - CinePass Movie Booking";

                String plainText =
                        "Hello " + userName + ",\n\n"
                        + "You have successfully logged in to your CinePass account.\n\n"
                        + "User Name: " + userName + "\n"
                        + "Registered Email: " + recipientEmail + "\n"
                        + "Login Date & Time: " + loginTime + "\n"
                        + "Authentication Status: SUCCESS\n\n"
                        + "If you did not initiate this login, please secure your account.\n\n"
                        + "CinePass Movie Booking";

                String htmlContent =
                        "<h1 style='background:#be123c;color:white;padding:20px;"
                        + "text-align:center'>CinePass</h1>"
                        + "<div style='font-family:Arial;padding:20px'>"
                        + "<h2 style='color:#059669'>Login Successful</h2>"
                        + "<p>Hello <b>" + escapeHtml(userName) + "</b>,</p>"
                        + "<p>You have successfully logged in to your CinePass account.</p>"
                        + "<table style='border-collapse:collapse;width:100%'>"
                        + row("User Name", userName)
                        + row("Registered Email", recipientEmail)
                        + row("Login Date & Time", loginTime)
                        + row("Authentication Status", "SUCCESS")
                        + "</table>"
                        + "<p>If you did not initiate this login, please secure your account.</p>"
                        + "<p>Thank you,<br>CinePass Support</p>"
                        + "</div>";

                sendEmail(recipientEmail, subject, plainText, htmlContent);

            } catch (Exception ex) {
                logger.error("Login email preparation failed: {}",
                        ex.getMessage(), ex);
            }
        });
    }

    // --------------------------------------------------
    // 2. BOOKING CONFIRMATION / E-TICKET
    // --------------------------------------------------

    public void sendBookingConfirmationTicket(Booking booking) {

        if (booking == null) {
            logger.warn("Cannot send booking email: booking is null.");
            return;
        }

        final String recipientEmail =
                (booking.getUserEmail() != null
                        && !booking.getUserEmail().isBlank())
                        ? booking.getUserEmail().trim().toLowerCase()
                        : (booking.getUser() != null
                            && booking.getUser().getEmail() != null)
                                ? booking.getUser().getEmail()
                                        .trim().toLowerCase()
                                : "";

        if (recipientEmail.isBlank()) {
            logger.warn("No recipient email for booking {}",
                    booking.getBookingNumber());
            return;
        }

        // Extract booking details before starting the asynchronous task.

        final String movieTitle =
                booking.getShow() != null
                && booking.getShow().getMovie() != null
                        ? booking.getShow().getMovie().getTitle()
                        : "Movie";

        final String movieGenre =
                booking.getShow() != null
                && booking.getShow().getMovie() != null
                        ? safe(booking.getShow().getMovie().getGenre())
                        : "";

        final String movieLanguage =
                booking.getShow() != null
                && booking.getShow().getMovie() != null
                        ? safe(booking.getShow().getMovie().getLanguage())
                        : "English";

        final String movieDuration =
                booking.getShow() != null
                && booking.getShow().getMovie() != null
                        ? safe(booking.getShow().getMovie().getDuration())
                        : "";

        final String theatreName =
                booking.getShow() != null
                && booking.getShow().getTheatre() != null
                        ? safe(booking.getShow().getTheatre().getName())
                        : "Cinema";

        final String theatreLocation =
                booking.getShow() != null
                && booking.getShow().getTheatre() != null
                        ? safe(booking.getShow().getTheatre().getLocation())
                        : "";

        final String showDate =
                booking.getShow() != null
                && booking.getShow().getShowDate() != null
                        ? booking.getShow().getShowDate().format(
                            DateTimeFormatter.ofPattern(
                                    "EEEE, dd MMMM yyyy", Locale.ENGLISH))
                        : "";

        final String showTime =
                booking.getShow() != null
                && booking.getShow().getShowTime() != null
                        ? safe(booking.getShow().getShowTime())
                        : "";

        final String screenName =
                booking.getShow() != null
                && booking.getShow().getScreenName() != null
                        ? safe(booking.getShow().getScreenName())
                        : "";

        final int seatCount =
                booking.getSeatCount() != null
                        ? booking.getSeatCount() : 0;

        final String seats =
                safe(booking.getSeatsDisplay());

        final double totalAmount =
                booking.getTotalAmount() != null
                        ? booking.getTotalAmount() : 0.0;

        final String bookingNumber =
                safe(booking.getBookingNumber());

        final String transactionId =
                safe(booking.getTransactionId());

        final String paymentMethod =
                booking.getPaymentMethod() != null
                        ? safe(booking.getPaymentMethod()) : "UPI";

        CompletableFuture.runAsync(() -> {
            try {
                String subject = "Booking Confirmed: E-Ticket for "
                        + movieTitle + " [" + bookingNumber + "]";

                String plainText =
                        "CINEPASS - E-TICKET CONFIRMATION\n\n"
                        + "Booking ID: " + bookingNumber + "\n"
                        + "Status: PAID / CONFIRMED\n\n"
                        + "Movie: " + movieTitle + "\n"
                        + "Genre: " + movieGenre + "\n"
                        + "Language: " + movieLanguage + "\n"
                        + "Duration: " + movieDuration + "\n\n"
                        + "Theatre: " + theatreName + "\n"
                        + "Location: " + theatreLocation + "\n"
                        + "Show Date: " + showDate + "\n"
                        + "Show Time: " + showTime + "\n"
                        + "Screen: " + screenName + "\n"
                        + "Number of Seats: " + seatCount + "\n"
                        + "Selected Seats: " + seats + "\n"
                        + "Total Price: Rs. "
                        + String.format(Locale.ROOT, "%.2f", totalAmount)
                        + "\n\n"
                        + "Payment Method: " + paymentMethod + "\n"
                        + "Transaction ID: " + transactionId + "\n"
                        + "Registered Email: " + recipientEmail + "\n\n"
                        + "Please present this e-ticket at the cinema entrance.\n"
                        + "Please arrive at least 15 minutes before showtime.\n\n"
                        + "Thank you for choosing CinePass!";

                String htmlContent =
                        "<div style='font-family:Arial;max-width:650px;"
                        + "margin:auto;color:#0f172a;border:1px solid #ddd'>"
                        + "<div style='background:#9f1239;color:white;"
                        + "padding:24px;text-align:center'>"
                        + "<h1>CinePass Official E-Ticket</h1>"
                        + "<p>Booking ID: <b>" + escapeHtml(bookingNumber)
                        + "</b></p>"
                        + "<h3>BOOKING & PAYMENT CONFIRMED</h3>"
                        + "</div>"
                        + "<div style='padding:24px'>"
                        + "<h2 style='color:#e11d48'>"
                        + escapeHtml(movieTitle) + "</h2>"
                        + "<p>" + escapeHtml(movieGenre) + " | "
                        + escapeHtml(movieLanguage) + " | "
                        + escapeHtml(movieDuration) + "</p>"
                        + "<table style='width:100%;border-collapse:collapse'>"
                        + row("Theatre Name", theatreName)
                        + row("Theatre Location", theatreLocation)
                        + row("Show Date", showDate)
                        + row("Show Time", showTime)
                        + row("Screen", screenName)
                        + row("Number of Seats", String.valueOf(seatCount))
                        + row("Selected Seat Numbers", seats)
                        + row("Total Ticket Price",
                                "Rs. " + String.format(
                                        Locale.ROOT, "%.2f", totalAmount))
                        + row("Payment Method", paymentMethod)
                        + row("Payment Status", "PAID / SUCCESS")
                        + row("Transaction ID", transactionId)
                        + row("Registered Email", recipientEmail)
                        + "</table>"
                        + "<p style='background:#fffbeb;padding:14px'>"
                        + "<b>Entry Instructions:</b> Please present this "
                        + "digital e-ticket at the cinema entrance. Arrive "
                        + "at least 15 minutes before showtime.</p>"
                        + "<p>Thank you for booking with CinePass!</p>"
                        + "</div>"
                        + "<div style='background:#f8fafc;padding:15px;"
                        + "text-align:center'>CinePass Movie Booking</div>"
                        + "</div>";

                sendEmail(recipientEmail, subject, plainText, htmlContent);

            } catch (Exception ex) {
                logger.error("Booking email preparation failed: {}",
                        ex.getMessage(), ex);
            }
        });
    }

    // --------------------------------------------------
    // 3. SEND EMAIL THROUGH BREVO HTTP API
    // --------------------------------------------------

    private void sendEmail(
            String toEmail,
            String subject,
            String plainText,
            String htmlBody) {

        if (toEmail == null || toEmail.isBlank()
                || !toEmail.contains("@")) {
            logger.warn("Email skipped: invalid recipient.");
            return;
        }

        if (!mailEnabled) {
            logger.warn("Email sending disabled by configuration.");
            return;
        }

        if (brevoApiKey == null || brevoApiKey.isBlank()) {
            logger.error("BREVO_API_KEY is missing.");
            return;
        }

        if (brevoFromEmail == null || brevoFromEmail.isBlank()) {
            logger.error("BREVO_FROM_EMAIL is missing.");
            return;
        }

        try {
            Map<String, Object> payload = Map.of(
                    "sender", Map.of(
                            "name", "CinePass Movie Booking",
                            "email", brevoFromEmail.trim()
                    ),
                    "to", List.of(
                            Map.of("email", toEmail.trim().toLowerCase())
                    ),
                    "subject", subject,
                    "textContent", plainText,
                    "htmlContent", htmlBody
            );

            String jsonBody = objectMapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BREVO_API_URL))
                    .header("accept", "application/json")
                    .header("api-key", brevoApiKey.trim())
                    .header("content-type", "application/json")
                    .timeout(java.time.Duration.ofSeconds(25))
                    .POST(HttpRequest.BodyPublishers.ofString(
                            jsonBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString(
                            StandardCharsets.UTF_8)
            );

            int statusCode = response.statusCode();

            if (statusCode >= 200 && statusCode < 300) {
                logger.info(
                        "Brevo accepted email request. Recipient: {}, HTTP status: {}",
                        toEmail, statusCode);
            } else {
                logger.error(
                        "Brevo email request failed. HTTP status: {}, response: {}",
                        statusCode, response.body());
            }

        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            logger.error("Brevo email request was interrupted.");
        } catch (Exception ex) {
            logger.error("Error sending email through Brevo: {}",
                    ex.getMessage(), ex);
        }
    }

    // --------------------------------------------------
    // 4. HELPER METHODS
    // --------------------------------------------------

    private String row(String label, String value) {
        return "<tr>"
                + "<td style='padding:10px;border-bottom:1px solid #e2e8f0;"
                + "color:#64748b'>" + escapeHtml(label) + "</td>"
                + "<td style='padding:10px;border-bottom:1px solid #e2e8f0;"
                + "font-weight:bold'>" + escapeHtml(safe(value)) + "</td>"
                + "</tr>";
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }

        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
