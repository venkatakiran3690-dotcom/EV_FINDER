package com.smartev.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class SessionStartRequest {

    @NotNull(message = "Booking ID is required")
    private Long bookingId;

    @Min(value = 1, message = "Initial battery percentage must be at least 1%")
    @Max(value = 95, message = "Initial battery percentage cannot exceed 95%")
    private Integer initialBatteryPct = 20;

    public SessionStartRequest() {}

    public SessionStartRequest(Long bookingId, Integer initialBatteryPct) {
        this.bookingId = bookingId;
        this.initialBatteryPct = initialBatteryPct;
    }

    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }

    public Integer getInitialBatteryPct() { return initialBatteryPct; }
    public void setInitialBatteryPct(Integer initialBatteryPct) { this.initialBatteryPct = initialBatteryPct; }
}
