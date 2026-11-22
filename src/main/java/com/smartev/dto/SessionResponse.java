package com.smartev.dto;

import java.time.LocalDateTime;

public class SessionResponse {

    private Long id;
    private String sessionCode;
    private Long bookingId;
    private String bookingCode;
    private Long chargerId;
    private String chargerIdentifier;
    private String chargerType;
    private Double powerRatingKw;
    private String stationName;
    private String stationAddress;
    private Long driverId;
    private String driverName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long elapsedMinutes;
    private Double energyConsumedKwh;
    private Integer initialBatteryPct;
    private Integer currentBatteryPct;
    private String status;
    private Long vehicleId;
    private String vehicleModel;
    private String vehicleMake;
    private String vehiclePlate;
    private Double batteryCapacityKwh;
    private Double estimatedFullRangeKm;
    private Integer estimatedRemainingMinutes;
    private Double estimatedDrivingRangeKm;

    public SessionResponse() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSessionCode() { return sessionCode; }
    public void setSessionCode(String sessionCode) { this.sessionCode = sessionCode; }

    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }

    public String getBookingCode() { return bookingCode; }
    public void setBookingCode(String bookingCode) { this.bookingCode = bookingCode; }

    public Long getChargerId() { return chargerId; }
    public void setChargerId(Long chargerId) { this.chargerId = chargerId; }

    public String getChargerIdentifier() { return chargerIdentifier; }
    public void setChargerIdentifier(String chargerIdentifier) { this.chargerIdentifier = chargerIdentifier; }

    public String getChargerType() { return chargerType; }
    public void setChargerType(String chargerType) { this.chargerType = chargerType; }

    public Double getPowerRatingKw() { return powerRatingKw; }
    public void setPowerRatingKw(Double powerRatingKw) { this.powerRatingKw = powerRatingKw; }

    public String getStationName() { return stationName; }
    public void setStationName(String stationName) { this.stationName = stationName; }

    public String getStationAddress() { return stationAddress; }
    public void setStationAddress(String stationAddress) { this.stationAddress = stationAddress; }

    public Long getDriverId() { return driverId; }
    public void setDriverId(Long driverId) { this.driverId = driverId; }

    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public Long getElapsedMinutes() { return elapsedMinutes; }
    public void setElapsedMinutes(Long elapsedMinutes) { this.elapsedMinutes = elapsedMinutes; }

    public Double getEnergyConsumedKwh() { return energyConsumedKwh; }
    public void setEnergyConsumedKwh(Double energyConsumedKwh) { this.energyConsumedKwh = energyConsumedKwh; }

    public Integer getInitialBatteryPct() { return initialBatteryPct; }
    public void setInitialBatteryPct(Integer initialBatteryPct) { this.initialBatteryPct = initialBatteryPct; }

    public Integer getCurrentBatteryPct() { return currentBatteryPct; }
    public void setCurrentBatteryPct(Integer currentBatteryPct) { this.currentBatteryPct = currentBatteryPct; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getVehicleId() { return vehicleId; }
    public void setVehicleId(Long vehicleId) { this.vehicleId = vehicleId; }

    public String getVehicleModel() { return vehicleModel; }
    public void setVehicleModel(String vehicleModel) { this.vehicleModel = vehicleModel; }

    public String getVehicleMake() { return vehicleMake; }
    public void setVehicleMake(String vehicleMake) { this.vehicleMake = vehicleMake; }

    public String getVehiclePlate() { return vehiclePlate; }
    public void setVehiclePlate(String vehiclePlate) { this.vehiclePlate = vehiclePlate; }

    public Double getBatteryCapacityKwh() { return batteryCapacityKwh; }
    public void setBatteryCapacityKwh(Double batteryCapacityKwh) { this.batteryCapacityKwh = batteryCapacityKwh; }

    public Double getEstimatedFullRangeKm() { return estimatedFullRangeKm; }
    public void setEstimatedFullRangeKm(Double estimatedFullRangeKm) { this.estimatedFullRangeKm = estimatedFullRangeKm; }

    public Integer getEstimatedRemainingMinutes() { return estimatedRemainingMinutes; }
    public void setEstimatedRemainingMinutes(Integer estimatedRemainingMinutes) { this.estimatedRemainingMinutes = estimatedRemainingMinutes; }

    public Double getEstimatedDrivingRangeKm() { return estimatedDrivingRangeKm; }
    public void setEstimatedDrivingRangeKm(Double estimatedDrivingRangeKm) { this.estimatedDrivingRangeKm = estimatedDrivingRangeKm; }
}
