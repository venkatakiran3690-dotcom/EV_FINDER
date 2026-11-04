package com.smartev.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "charging_sessions", indexes = {
    @Index(name = "idx_session_booking", columnList = "booking_id", unique = true)
})
public class ChargingSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "charger_id", nullable = false)
    private Charger charger;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    private User driver;

    @Column(name = "session_code", nullable = false, unique = true, length = 40)
    private String sessionCode;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "energy_consumed_kwh", nullable = false)
    private Double energyConsumedKwh = 0.0;

    @Column(name = "initial_battery_pct", nullable = false)
    private Integer initialBatteryPct = 20;

    @Column(name = "current_battery_pct", nullable = false)
    private Integer currentBatteryPct = 20;

    @Column(nullable = false, length = 30)
    private String status = "ACTIVE"; // ACTIVE, COMPLETED, STOPPED_EARLY

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public ChargingSession() {}

    public ChargingSession(Booking booking, Charger charger, User driver, String sessionCode,
                           LocalDateTime startTime, Integer initialBatteryPct) {
        this.booking = booking;
        this.charger = charger;
        this.driver = driver;
        this.sessionCode = sessionCode;
        this.startTime = startTime;
        this.initialBatteryPct = initialBatteryPct;
        this.currentBatteryPct = initialBatteryPct;
        this.energyConsumedKwh = 0.0;
        this.status = "ACTIVE";
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }

    public Charger getCharger() { return charger; }
    public void setCharger(Charger charger) { this.charger = charger; }

    public User getDriver() { return driver; }
    public void setDriver(User driver) { this.driver = driver; }

    public String getSessionCode() { return sessionCode; }
    public void setSessionCode(String sessionCode) { this.sessionCode = sessionCode; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public Double getEnergyConsumedKwh() { return energyConsumedKwh; }
    public void setEnergyConsumedKwh(Double energyConsumedKwh) { this.energyConsumedKwh = energyConsumedKwh; }

    public Integer getInitialBatteryPct() { return initialBatteryPct; }
    public void setInitialBatteryPct(Integer initialBatteryPct) { this.initialBatteryPct = initialBatteryPct; }

    public Integer getCurrentBatteryPct() { return currentBatteryPct; }
    public void setCurrentBatteryPct(Integer currentBatteryPct) { this.currentBatteryPct = currentBatteryPct; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
