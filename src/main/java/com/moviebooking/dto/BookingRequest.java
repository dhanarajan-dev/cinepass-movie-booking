package com.moviebooking.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class BookingRequest {

    @NotNull(message = "Show ID is required")
    private Long showId;

    @NotEmpty(message = "At least one seat must be selected")
    private List<String> selectedSeats;

    @NotNull(message = "Payment method is required")
    private String paymentMethod;

    private boolean termsAccepted;

    public BookingRequest() {
    }

    public BookingRequest(Long showId, List<String> selectedSeats, String paymentMethod, boolean termsAccepted) {
        this.showId = showId;
        this.selectedSeats = selectedSeats;
        this.paymentMethod = paymentMethod;
        this.termsAccepted = termsAccepted;
    }

    public Long getShowId() {
        return showId;
    }

    public void setShowId(Long showId) {
        this.showId = showId;
    }

    public List<String> getSelectedSeats() {
        return selectedSeats;
    }

    public void setSelectedSeats(List<String> selectedSeats) {
        this.selectedSeats = selectedSeats;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public boolean isTermsAccepted() {
        return termsAccepted;
    }

    public void setTermsAccepted(boolean termsAccepted) {
        this.termsAccepted = termsAccepted;
    }
}
