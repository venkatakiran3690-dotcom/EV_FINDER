package com.smartev.dto;

import jakarta.validation.constraints.NotNull;

public class PaymentRequest {

    @NotNull(message = "Booking ID is required")
    private Long bookingId;

    private boolean simulateFailure = false;

    private String paymentMethodNote = "Demo Token Payment (Simulated)";

    public PaymentRequest() {}

    public PaymentRequest(Long bookingId, boolean simulateFailure, String paymentMethodNote) {
        this.bookingId = bookingId;
        this.simulateFailure = simulateFailure;
        this.paymentMethodNote = paymentMethodNote;
    }

    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }

    public boolean isSimulateFailure() { return simulateFailure; }
    public void setSimulateFailure(boolean simulateFailure) { this.simulateFailure = simulateFailure; }

    public String getPaymentMethodNote() { return paymentMethodNote; }
    public void setPaymentMethodNote(String paymentMethodNote) { this.paymentMethodNote = paymentMethodNote; }
}
