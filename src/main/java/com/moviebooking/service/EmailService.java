package com.moviebooking.service;

import com.moviebooking.model.Booking;
import com.moviebooking.model.User;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${app.mail.enabled:true}")
    private boolean mailEnabled;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${app.mail.from:${spring.mail.username:}}")
    private String fromEmail;

    /**
     * Requirement: After a successful login, a login success notification should be sent 
     * dynamically to the user's original registered email address.
     * Executes asynchronously so login response is instant.
     */
    public void sendLoginNotification(User user) {
        if (user == null || user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            logger.warn("Cannot send login notification: user or email is null/empty.");
            return;
        }

        // Dynamically capture the user's registered email and name
        final String recipientEmail = user.getEmail().trim().toLowerCase();
        final String userName = (user.getName() != null && !user.getName().trim().isEmpty()) ? user.getName().trim() : "Valued User";
        final String timeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy, hh:mm:ss a"));

        CompletableFuture.runAsync(() -> {
            try {
                String subject = "Login Success Notification - CinePass Movie Booking";

                String htmlContent = """
                    <div style="font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; max-width: 600px; margin: 0 auto; background: #ffffff; color: #0f172a; border-radius: 12px; overflow: hidden; border: 1px solid #e2e8f0; box-shadow: 0 10px 25px rgba(0,0,0,0.06);">
                        <!-- Header -->
                        <div style="background: linear-gradient(135deg, #e11d48, #be123c); padding: 25px; text-align: center;">
                            <h1 style="margin: 0; color: #ffffff; font-size: 24px; letter-spacing: 0.5px; font-weight: 800;">🎬 CinePass</h1>
                            <p style="margin: 5px 0 0; color: #ffe4e6; font-size: 14px;">Online Movie Ticket Booking System</p>
                        </div>

                        <!-- Body -->
                        <div style="padding: 30px 25px; background: #ffffff;">
                            <div style="display: inline-block; background: #ecfdf5; border: 1px solid #a7f3d0; color: #059669; font-size: 12px; font-weight: 700; padding: 4px 12px; border-radius: 50px; margin-bottom: 14px;">
                                ✓ LOGIN SUCCESSFUL
                            </div>
                            <h2 style="color: #0f172a; margin: 0 0 12px; font-size: 20px;">Login Success Notification</h2>
                            <p style="color: #334155; font-size: 15px; line-height: 1.6; margin: 0 0 15px;">Hello <strong>%s</strong>,</p>
                            <p style="color: #334155; font-size: 15px; line-height: 1.6; margin: 0 0 20px;">
                                You have successfully logged in to your <strong>CinePass</strong> account. Below are your login session details:
                            </p>

                            <!-- Details Card -->
                            <div style="background: #f8fafc; border-left: 4px solid #059669; border-top: 1px solid #e2e8f0; border-right: 1px solid #e2e8f0; border-bottom: 1px solid #e2e8f0; padding: 18px 20px; border-radius: 6px; margin: 20px 0;">
                                <table style="width: 100%%; border-collapse: collapse; font-size: 14px; color: #334155;">
                                    <tr>
                                        <td style="padding: 7px 0; color: #64748b; width: 45%%;">User Name:</td>
                                        <td style="padding: 7px 0; color: #0f172a; font-weight: 700;">%s</td>
                                    </tr>
                                    <tr>
                                        <td style="padding: 7px 0; color: #64748b;">Registered Email:</td>
                                        <td style="padding: 7px 0; color: #0284c7; font-weight: 700;">%s</td>
                                    </tr>
                                    <tr>
                                        <td style="padding: 7px 0; color: #64748b;">Login Date & Time:</td>
                                        <td style="padding: 7px 0; color: #0f172a; font-weight: 700;">%s</td>
                                    </tr>
                                    <tr>
                                        <td style="padding: 7px 0; color: #64748b;">Authentication Status:</td>
                                        <td style="padding: 7px 0; color: #059669; font-weight: 700;">AUTHENTICATED (SUCCESS)</td>
                                    </tr>
                                </table>
                            </div>

                            <p style="color: #64748b; font-size: 13px; line-height: 1.6; margin-top: 20px;">
                                If you did not initiate this login session, please secure your account immediately by resetting your password.
                            </p>
                        </div>

                        <!-- Footer -->
                        <div style="background: #f8fafc; padding: 15px 25px; text-align: center; border-top: 1px solid #e2e8f0;">
                            <p style="margin: 0; color: #64748b; font-size: 12px;">&copy; 2026 CinePass Entertainment Inc. All rights reserved.</p>
                        </div>
                    </div>
                    """.formatted(userName, userName, recipientEmail, timeStr);

                String plainText = String.format(
                    "Hello %s,\n\n" +
                    "You have successfully logged in to your CinePass account.\n\n" +
                    "Login Session Details:\n" +
                    "- User Name: %s\n" +
                    "- Registered Email: %s\n" +
                    "- Login Date & Time: %s\n" +
                    "- Authentication Status: AUTHENTICATED (SUCCESS)\n\n" +
                    "If you did not initiate this login session, please secure your account immediately.\n\n" +
                    "© 2026 CinePass Entertainment Inc. All rights reserved.",
                    userName, userName, recipientEmail, timeStr
                );

                sendEmail(recipientEmail, subject, plainText, htmlContent);
            } catch (Exception ex) {
                logger.error("Error in async sendLoginNotification for [{}]: {}", recipientEmail, ex.getMessage(), ex);
            }
        });
    }

    /**
     * Requirement: After the payment is successfully completed and the booking is confirmed, 
     * an e-ticket confirmation email must be sent dynamically to the user's original registered email address.
     * Contains:
     * 1. Movie name
     * 2. Theatre name
     * 3. Location
     * 4. Show date and time
     * 5. Selected seat numbers
     * 6. Number of seats
     * 7. Total price
     */
    public void sendBookingConfirmationTicket(Booking booking) {
        if (booking == null) {
            logger.warn("Cannot send booking confirmation: booking entity is null.");
            return;
        }

        // Dynamically extract recipient email from booking (from stored user_email column or user relation)
        final String recipientEmail = (booking.getUserEmail() != null && !booking.getUserEmail().trim().isEmpty())
                ? booking.getUserEmail().trim().toLowerCase()
                : (booking.getUser() != null && booking.getUser().getEmail() != null
                    ? booking.getUser().getEmail().trim().toLowerCase()
                    : "");

        if (recipientEmail.isEmpty()) {
            logger.warn("Cannot send booking confirmation: no recipient email found for booking {}", booking.getBookingNumber());
            return;
        }

        // Extract all required fields eagerly before async thread dispatch
        final String movieTitle = (booking.getShow() != null && booking.getShow().getMovie() != null)
                ? booking.getShow().getMovie().getTitle() : "Movie";
        final String movieGenre = (booking.getShow() != null && booking.getShow().getMovie() != null)
                ? booking.getShow().getMovie().getGenre() : "";
        final String movieLanguage = (booking.getShow() != null && booking.getShow().getMovie() != null)
                ? booking.getShow().getMovie().getLanguage() : "English";
        final String movieDuration = (booking.getShow() != null && booking.getShow().getMovie() != null)
                ? booking.getShow().getMovie().getDuration() : "";
        final String theatreName = (booking.getShow() != null && booking.getShow().getTheatre() != null)
                ? booking.getShow().getTheatre().getName() : "Cinema";
        final String theatreLocation = (booking.getShow() != null && booking.getShow().getTheatre() != null)
                ? booking.getShow().getTheatre().getLocation() : "";
        final String showDateFormatted = (booking.getShow() != null && booking.getShow().getShowDate() != null)
                ? booking.getShow().getShowDate().format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")) : "";
        final String showTime = (booking.getShow() != null && booking.getShow().getShowTime() != null)
                ? booking.getShow().getShowTime() : "";
        final String screenName = (booking.getShow() != null && booking.getShow().getScreenName() != null)
                ? booking.getShow().getScreenName() : "";
        final int seatCount = (booking.getSeatCount() != null) ? booking.getSeatCount() : 0;
        final String selectedSeatNumbers = (booking.getSeatsDisplay() != null) ? booking.getSeatsDisplay() : "";
        final double totalAmount = (booking.getTotalAmount() != null) ? booking.getTotalAmount() : 0.0;
        final String bookingNumber = (booking.getBookingNumber() != null) ? booking.getBookingNumber() : "";
        final String transactionId = (booking.getTransactionId() != null) ? booking.getTransactionId() : "";
        final String paymentMethod = (booking.getPaymentMethod() != null) ? booking.getPaymentMethod() : "UPI";

        CompletableFuture.runAsync(() -> {
            try {
                String subject = "Booking Confirmed: E-Ticket for " + movieTitle + " [" + bookingNumber + "]";

                String htmlContent = """
                    <div style="font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; max-width: 650px; margin: 0 auto; background: #ffffff; color: #0f172a; border-radius: 14px; overflow: hidden; border: 1px solid #e2e8f0; box-shadow: 0 10px 30px rgba(0,0,0,0.08);">
                        <!-- Header Banner -->
                        <div style="background: linear-gradient(135deg, #e11d48, #9f1239); padding: 25px 30px; text-align: center;">
                            <div style="display: inline-block; background: rgba(255,255,255,0.25); border-radius: 50px; padding: 6px 18px; margin-bottom: 10px;">
                                <span style="color: #ffffff; font-size: 13px; font-weight: 700; letter-spacing: 0.5px;">✓ BOOKING & PAYMENT CONFIRMED</span>
                            </div>
                            <h1 style="margin: 0; color: #ffffff; font-size: 26px; letter-spacing: 0.5px; font-weight: 800;">🎬 CinePass Official E-Ticket</h1>
                            <p style="margin: 6px 0 0; color: #ffe4e6; font-size: 14px;">Booking ID: <strong>%s</strong></p>
                        </div>

                        <!-- Ticket Body -->
                        <div style="padding: 30px; background: #ffffff;">
                            <!-- Movie Title Block -->
                            <div style="border-bottom: 2px dashed #cbd5e1; padding-bottom: 20px; margin-bottom: 20px;">
                                <div style="font-size: 12px; color: #64748b; text-transform: uppercase; letter-spacing: 1px; margin-bottom: 4px; font-weight: 700;">MOVIE NAME</div>
                                <h2 style="color: #e11d48; margin: 0 0 8px; font-size: 26px; font-weight: 800;">%s</h2>
                                <p style="margin: 0; color: #475569; font-size: 14px;">
                                    %s | %s | %s
                                </p>
                            </div>

                            <!-- 7 Required Ticket Confirmation Details -->
                            <table style="width: 100%%; border-collapse: collapse; margin-bottom: 25px;">
                                <tr>
                                    <td style="padding: 10px 0; color: #64748b; font-size: 14px; width: 45%%;">Theatre Name:</td>
                                    <td style="padding: 10px 0; color: #0f172a; font-size: 15px; font-weight: 700;">%s</td>
                                </tr>
                                <tr>
                                    <td style="padding: 10px 0; color: #64748b; font-size: 14px;">Theatre Location:</td>
                                    <td style="padding: 10px 0; color: #334155; font-size: 14px;">%s</td>
                                </tr>
                                <tr>
                                    <td style="padding: 10px 0; color: #64748b; font-size: 14px;">Show Date & Time:</td>
                                    <td style="padding: 10px 0; color: #0284c7; font-size: 15px; font-weight: 700;">%s at %s (%s)</td>
                                </tr>
                                <tr>
                                    <td style="padding: 10px 0; color: #64748b; font-size: 14px;">Number of Seats Booked:</td>
                                    <td style="padding: 10px 0; color: #0f172a; font-size: 15px; font-weight: 700;">%d Seat(s)</td>
                                </tr>
                                <tr>
                                    <td style="padding: 10px 0; color: #64748b; font-size: 14px;">Selected Seat Numbers:</td>
                                    <td style="padding: 10px 0; color: #059669; font-size: 18px; font-weight: 800; letter-spacing: 1px;">%s</td>
                                </tr>
                                <tr style="border-top: 1px solid #e2e8f0; border-bottom: 1px solid #e2e8f0;">
                                    <td style="padding: 14px 0; color: #0f172a; font-size: 15px; font-weight: 700;">Total Ticket Price Paid:</td>
                                    <td style="padding: 14px 0; color: #d97706; font-size: 20px; font-weight: 800;">₹%.2f</td>
                                </tr>
                            </table>

                            <!-- Payment Information -->
                            <div style="background: #f8fafc; border: 1px solid #e2e8f0; padding: 15px 20px; border-radius: 8px; margin-bottom: 25px;">
                                <h4 style="margin: 0 0 10px; color: #64748b; font-size: 12px; text-transform: uppercase; letter-spacing: 0.5px;">Payment Summary</h4>
                                <div style="font-size: 13px; color: #334155; margin-bottom: 4px;">
                                    <span>Payment Method: <strong>%s</strong></span> • <span>Payment Status: <strong style="color: #059669;">PAID (SUCCESS)</strong></span>
                                </div>
                                <div style="font-size: 13px; color: #64748b;">
                                    <span>Transaction ID: <code style="background: #e2e8f0; padding: 2px 6px; border-radius: 4px; color: #0f172a;">%s</code></span>
                                </div>
                            </div>

                            <!-- Admission Instructions -->
                            <div style="background: #fffbeb; border-left: 4px solid #f59e0b; border-top: 1px solid #fef3c7; border-right: 1px solid #fef3c7; border-bottom: 1px solid #fef3c7; padding: 12px 16px; border-radius: 4px;">
                                <p style="margin: 0; color: #92400e; font-size: 13px; line-height: 1.5;">
                                    <strong>Entry Instructions:</strong> Please present this digital e-ticket on your mobile phone at the auditorium entrance. Please arrive at least 15 minutes before the showtime.
                                </p>
                            </div>
                        </div>

                        <!-- Footer -->
                        <div style="background: #f8fafc; padding: 18px 30px; text-align: center; border-top: 1px solid #e2e8f0;">
                            <p style="margin: 0 0 5px; color: #475569; font-size: 13px;">
                                Delivered to registered email: <strong>%s</strong>
                            </p>
                            <p style="margin: 0; color: #64748b; font-size: 12px;">&copy; 2026 CinePass Entertainment Inc. Thank you for booking with us!</p>
                        </div>
                    </div>
                    """.formatted(
                        bookingNumber,
                        movieTitle,
                        movieGenre,
                        movieLanguage,
                        movieDuration,
                        theatreName,
                        theatreLocation,
                        showDateFormatted,
                        showTime,
                        screenName,
                        seatCount,
                        selectedSeatNumbers,
                        totalAmount,
                        paymentMethod,
                        transactionId,
                        recipientEmail
                );

                String plainText = String.format(
                    "CINEPASS - OFFICIAL E-TICKET CONFIRMATION\n" +
                    "=========================================\n" +
                    "Booking Reference ID: %s\n" +
                    "Status: PAID (CONFIRMED)\n\n" +
                    "MOVIE DETAILS:\n" +
                    "- Movie Name: %s\n" +
                    "- Details: %s | %s | %s\n\n" +
                    "THEATRE & SHOW DETAILS:\n" +
                    "- Theatre Name: %s\n" +
                    "- Theatre Location: %s\n" +
                    "- Show Date & Time: %s at %s (%s)\n" +
                    "- Number of Seats Booked: %d Seat(s)\n" +
                    "- Selected Seat Numbers: %s\n" +
                    "- Total Ticket Price Paid: Rs. %.2f\n\n" +
                    "PAYMENT DETAILS:\n" +
                    "- Payment Method: %s\n" +
                    "- Transaction ID: %s\n" +
                    "- Registered Email: %s\n\n" +
                    "ENTRY INSTRUCTIONS:\n" +
                    "Please present your digital Booking Reference (%s) or this e-ticket at the cinema entrance.\n" +
                    "Please arrive at least 15 minutes before showtime.\n\n" +
                    "Thank you for choosing CinePass!\n" +
                    "© 2026 CinePass Entertainment Inc.",
                    bookingNumber,
                    movieTitle,
                    movieGenre, movieLanguage, movieDuration,
                    theatreName,
                    theatreLocation,
                    showDateFormatted, showTime, screenName,
                    seatCount,
                    selectedSeatNumbers,
                    totalAmount,
                    paymentMethod,
                    transactionId,
                    recipientEmail,
                    bookingNumber
                );

                sendEmail(recipientEmail, subject, plainText, htmlContent);
            } catch (Exception ex) {
                logger.error("Error in async sendBookingConfirmationTicket for [{}]: {}", recipientEmail, ex.getMessage(), ex);
            }
        });
    }

    private void sendEmail(String toEmail, String subject, String plainText, String htmlBody) {
        if (toEmail == null || toEmail.trim().isEmpty() || !toEmail.contains("@")) {
            logger.warn(">>> Skipped email dispatch: recipient address is invalid: '{}'", toEmail);
            return;
        }

        final String cleanToEmail = toEmail.trim().toLowerCase();
        final String senderAddress = (fromEmail != null && !fromEmail.isBlank())
                ? fromEmail.trim()
                : (mailUsername != null && !mailUsername.isBlank() ? mailUsername.trim() : "noreply@cinepass.com");

        logger.info("================== DYNAMIC EMAIL NOTIFICATION DISPATCH ==================");
        logger.info("FROM (CONFIGURED SMTPS): {}", senderAddress);
        logger.info("TO (USER REGISTERED EMAIL): {}", cleanToEmail);
        logger.info("SUBJECT: {}", subject);
        logger.info("MAIL ENABLED: {}", mailEnabled);

        if (mailEnabled && mailSender != null) {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                helper.setFrom(senderAddress, "CinePass Movie Booking");
                try {
                    helper.setReplyTo(senderAddress, "CinePass Support");
                } catch (Exception ignored) {}
                helper.setTo(cleanToEmail);
                helper.setSubject(subject);
                helper.setText(plainText, htmlBody);
                mailSender.send(message);
                logger.info(">>> SUCCESS: Real email successfully delivered via SMTP to registered user: {}", cleanToEmail);
                logger.info("=========================================================================");
                return;
            } catch (Exception e) {
                logger.error(">>> ERROR sending email via SMTP to [{}]: {}", cleanToEmail, e.getMessage(), e);
                logger.info(">>> NOTICE: Live SMTP delivery attempted using configured Gmail SMTP credentials.");
            }
        } else {
            logger.warn(">>> SMTP sending is disabled (app.mail.enabled={}) or mailSender is null.", mailEnabled);
        }

        logger.info(">>> Email notification captured in application logs for [{}]. Subject: [{}]", cleanToEmail, subject);
        logger.info("=========================================================================");
    }
}
