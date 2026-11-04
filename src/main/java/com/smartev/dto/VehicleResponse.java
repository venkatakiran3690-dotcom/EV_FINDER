package com.smartev.dto;

import java.time.LocalDateTime;

public class VehicleResponse {

    private Long id;
    private Long driverId;
    private String make;
    private String model;
    private Integer year;
    private String licensePlate;
    private Double batteryCapacityKwh;
    private String compatibleConnector;
    private LocalDateTime createdAt;

    public VehicleResponse() {}

    public VehicleResponse(Long id, Long driverId, String make, String model, Integer year,
                           String licensePlate, Double batteryCapacityKwh, String compatibleConnector,
                           LocalDateTime createdAt) {
        this.id = id;
        this.driverId = driverId;
        this.make = make;
        this.model = model;
        this.year = year;
        this.licensePlate = licensePlate;
        this.batteryCapacityKwh = batteryCapacityKwh;
        this.compatibleConnector = compatibleConnector;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDriverId() { return driverId; }
    public void setDriverId(Long driverId) { this.driverId = driverId; }

    public String getMake() { return make; }
    public void setMake(String make) { this.make = make; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }

    public Double getBatteryCapacityKwh() { return batteryCapacityKwh; }
    public void setBatteryCapacityKwh(Double batteryCapacityKwh) { this.batteryCapacityKwh = batteryCapacityKwh; }

    public String getCompatibleConnector() { return compatibleConnector; }
    public void setCompatibleConnector(String compatibleConnector) { this.compatibleConnector = compatibleConnector; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
