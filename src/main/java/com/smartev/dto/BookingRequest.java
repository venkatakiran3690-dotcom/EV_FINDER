package com.smartev.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class BookingRequest {

    @NotNull(message = "Station ID is required")
    private Long stationId;

    @NotNull(message = "Charger ID is required")
    private Long chargerId;

    private Long vehicleId;

    @NotNull(message = "Start time is required")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startTime;

    @NotNull(message = "Duration in minutes is required")
    @Min(value = 15, message = "Minimum booking duration is 15 minutes")
    @Max(value = 360, message = "Maximum booking duration is 6 hours (360 minutes)")
    private Integer durationMinutes;

    @NotNull(message = "Driver latitude is required for geofence validation")
    @Min(value = -90, message = "Invalid latitude")
    @Max(value = 90, message = "Invalid latitude")
    private Double driverLatitude;

    @NotNull(message = "Driver longitude is required for geofence validation")
    @Min(value = -180, message = "Invalid longitude")
    @Max(value = 180, message = "Invalid longitude")
    private Double driverLongitude;

    public BookingRequest() {}

    public Long getStationId() { return stationId; }
    public void setStationId(Long stationId) { this.stationId = stationId; }

    public Long getChargerId() { return chargerId; }
    public void setChargerId(Long chargerId) { this.chargerId = chargerId; }

    public Long getVehicleId() { return vehicleId; }
    public void setVehicleId(Long vehicleId) { this.vehicleId = vehicleId; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public Double getDriverLatitude() { return driverLatitude; }
    public void setDriverLatitude(Double driverLatitude) { this.driverLatitude = driverLatitude; }

    public Double getDriverLongitude() { return driverLongitude; }
    public void setDriverLongitude(Double driverLongitude) { this.driverLongitude = driverLongitude; }
}
