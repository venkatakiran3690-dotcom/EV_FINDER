package com.smartev.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings", indexes = {
    @Index(name = "idx_booking_charger_time", columnList = "charger_id, start_time, end_time"),
    @Index(name = "idx_booking_code", columnList = "booking_code")
})
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_code", nullable = false, unique = true, length = 40)
    private String bookingCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    private User driver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id", nullable = false)
    private Station station;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "charger_id", nullable = false)
    private Charger charger;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "calculated_distance_km", nullable = false)
    private Double calculatedDistanceKm;

    @Column(name = "token_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal tokenAmount;

    @Column(nullable = false, length = 30)
    private String status = "PENDING_PAYMENT"; // PENDING_PAYMENT, CONFIRMED, IN_PROGRESS, COMPLETED, CANCELLED, EXPIRED

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Booking() {}

    public Booking(String bookingCode, User driver, Vehicle vehicle, Station station, Charger charger,
                   LocalDateTime startTime, LocalDateTime endTime, Double calculatedDistanceKm, BigDecimal tokenAmount) {
        this.bookingCode = bookingCode;
        this.driver = driver;
        this.vehicle = vehicle;
        this.station = station;
        this.charger = charger;
        this.startTime = startTime;
        this.endTime = endTime;
        this.calculatedDistanceKm = calculatedDistanceKm;
        this.tokenAmount = tokenAmount;
        this.status = "PENDING_PAYMENT";
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBookingCode() { return bookingCode; }
    public void setBookingCode(String bookingCode) { this.bookingCode = bookingCode; }

    public User getDriver() { return driver; }
    public void setDriver(User driver) { this.driver = driver; }

    public Vehicle getVehicle() { return vehicle; }
    public void setVehicle(Vehicle vehicle) { this.vehicle = vehicle; }

    public Station getStation() { return station; }
    public void setStation(Station station) { this.station = station; }

    public Charger getCharger() { return charger; }
    public void setCharger(Charger charger) { this.charger = charger; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public Double getCalculatedDistanceKm() { return calculatedDistanceKm; }
    public void setCalculatedDistanceKm(Double calculatedDistanceKm) { this.calculatedDistanceKm = calculatedDistanceKm; }

    public BigDecimal getTokenAmount() { return tokenAmount; }
    public void setTokenAmount(BigDecimal tokenAmount) { this.tokenAmount = tokenAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
