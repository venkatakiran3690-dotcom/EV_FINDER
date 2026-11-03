package com.smartev.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class StationResponse {

    private Long id;
    private Long companyId;
    private String companyName;
    private String name;
    private String address;
    private String city;
    private String state;
    private String postalCode;
    private Double latitude;
    private Double longitude;
    private Integer totalChargers;
    private String operationalStatus;

    // Computed / Dynamic fields
    private Double distanceKm;
    private Double occupancyPercentage;
    private Long availableChargersCount;
    private Long operationalChargersCount;
    private Integer estimatedWaitMinutes;
    private Double averageRating;
    private BigDecimal baseTariffPerKwh;
    private List<ChargerResponse> chargers = new ArrayList<>();

    public StationResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Integer getTotalChargers() { return totalChargers; }
    public void setTotalChargers(Integer totalChargers) { this.totalChargers = totalChargers; }

    public String getOperationalStatus() { return operationalStatus; }
    public void setOperationalStatus(String operationalStatus) { this.operationalStatus = operationalStatus; }

    public Double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }

    public Double getOccupancyPercentage() { return occupancyPercentage; }
    public void setOccupancyPercentage(Double occupancyPercentage) { this.occupancyPercentage = occupancyPercentage; }

    public Long getAvailableChargersCount() { return availableChargersCount; }
    public void setAvailableChargersCount(Long availableChargersCount) { this.availableChargersCount = availableChargersCount; }

    public Long getOperationalChargersCount() { return operationalChargersCount; }
    public void setOperationalChargersCount(Long operationalChargersCount) { this.operationalChargersCount = operationalChargersCount; }

    public Integer getEstimatedWaitMinutes() { return estimatedWaitMinutes; }
    public void setEstimatedWaitMinutes(Integer estimatedWaitMinutes) { this.estimatedWaitMinutes = estimatedWaitMinutes; }

    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }

    public BigDecimal getBaseTariffPerKwh() { return baseTariffPerKwh; }
    public void setBaseTariffPerKwh(BigDecimal baseTariffPerKwh) { this.baseTariffPerKwh = baseTariffPerKwh; }

    public List<ChargerResponse> getChargers() { return chargers; }
    public void setChargers(List<ChargerResponse> chargers) { this.chargers = chargers; }
}
