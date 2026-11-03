package com.smartev.dto;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class DashboardStatsResponse {

    private String role;
    private Long totalDrivers = 0L;
    private Long totalCompanies = 0L;
    private Long totalStations = 0L;
    private Long totalChargers = 0L;
    private Long availableChargers = 0L;
    private Long occupiedChargers = 0L;
    private Long outOfServiceChargers = 0L;

    private Long totalBookings = 0L;
    private Long todayBookings = 0L;
    private Long activeSessions = 0L;
    private Long completedSessions = 0L;

    private BigDecimal totalRevenue = BigDecimal.ZERO;
    private Double totalEnergyKwh = 0.0;
    private Double estimatedCo2SavedKg = 0.0; // Clear academic formula: kWh * 0.82 kg CO2/kWh grid emission factor

    private Long pendingComplaints = 0L;
    private Long pendingMaintenance = 0L;
    private Long unreadNotifications = 0L;

    private Map<String, Object> additionalMetrics = new HashMap<>();

    public DashboardStatsResponse() {}

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Long getTotalDrivers() { return totalDrivers; }
    public void setTotalDrivers(Long totalDrivers) { this.totalDrivers = totalDrivers; }

    public Long getTotalCompanies() { return totalCompanies; }
    public void setTotalCompanies(Long totalCompanies) { this.totalCompanies = totalCompanies; }

    public Long getTotalStations() { return totalStations; }
    public void setTotalStations(Long totalStations) { this.totalStations = totalStations; }

    public Long getTotalChargers() { return totalChargers; }
    public void setTotalChargers(Long totalChargers) { this.totalChargers = totalChargers; }

    public Long getAvailableChargers() { return availableChargers; }
    public void setAvailableChargers(Long availableChargers) { this.availableChargers = availableChargers; }

    public Long getOccupiedChargers() { return occupiedChargers; }
    public void setOccupiedChargers(Long occupiedChargers) { this.occupiedChargers = occupiedChargers; }

    public Long getOutOfServiceChargers() { return outOfServiceChargers; }
    public void setOutOfServiceChargers(Long outOfServiceChargers) { this.outOfServiceChargers = outOfServiceChargers; }

    public Long getTotalBookings() { return totalBookings; }
    public void setTotalBookings(Long totalBookings) { this.totalBookings = totalBookings; }

    public Long getTodayBookings() { return todayBookings; }
    public void setTodayBookings(Long todayBookings) { this.todayBookings = todayBookings; }

    public Long getActiveSessions() { return activeSessions; }
    public void setActiveSessions(Long activeSessions) { this.activeSessions = activeSessions; }

    public Long getCompletedSessions() { return completedSessions; }
    public void setCompletedSessions(Long completedSessions) { this.completedSessions = completedSessions; }

    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }

    public Double getTotalEnergyKwh() { return totalEnergyKwh; }
    public void setTotalEnergyKwh(Double totalEnergyKwh) { this.totalEnergyKwh = totalEnergyKwh; }

    public Double getEstimatedCo2SavedKg() { return estimatedCo2SavedKg; }
    public void setEstimatedCo2SavedKg(Double estimatedCo2SavedKg) { this.estimatedCo2SavedKg = estimatedCo2SavedKg; }

    public Long getPendingComplaints() { return pendingComplaints; }
    public void setPendingComplaints(Long pendingComplaints) { this.pendingComplaints = pendingComplaints; }

    public Long getPendingMaintenance() { return pendingMaintenance; }
    public void setPendingMaintenance(Long pendingMaintenance) { this.pendingMaintenance = pendingMaintenance; }

    public Long getUnreadNotifications() { return unreadNotifications; }
    public void setUnreadNotifications(Long unreadNotifications) { this.unreadNotifications = unreadNotifications; }

    public Map<String, Object> getAdditionalMetrics() { return additionalMetrics; }
    public void setAdditionalMetrics(Map<String, Object> additionalMetrics) { this.additionalMetrics = additionalMetrics; }
}
