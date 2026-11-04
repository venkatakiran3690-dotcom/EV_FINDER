package com.smartev.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class TariffRequest {

    private Long stationId; // Optional: null = company wide

    @NotBlank(message = "Charger type is required (AC, DC, ALL)")
    private String chargerType = "ALL";

    @NotNull(message = "Price per kWh is required")
    @DecimalMin(value = "0.00", message = "Price per kWh cannot be negative")
    private BigDecimal pricePerKwh;

    @NotNull(message = "Service charge is required")
    @DecimalMin(value = "0.00", message = "Service charge cannot be negative")
    private BigDecimal serviceCharge;

    @NotNull(message = "Tax percentage is required")
    @DecimalMin(value = "0.00", message = "Tax percentage cannot be negative")
    private BigDecimal taxPercentage;

    public TariffRequest() {}

    public Long getStationId() { return stationId; }
    public void setStationId(Long stationId) { this.stationId = stationId; }

    public String getChargerType() { return chargerType; }
    public void setChargerType(String chargerType) { this.chargerType = chargerType; }

    public BigDecimal getPricePerKwh() { return pricePerKwh; }
    public void setPricePerKwh(BigDecimal pricePerKwh) { this.pricePerKwh = pricePerKwh; }

    public BigDecimal getServiceCharge() { return serviceCharge; }
    public void setServiceCharge(BigDecimal serviceCharge) { this.serviceCharge = serviceCharge; }

    public BigDecimal getTaxPercentage() { return taxPercentage; }
    public void setTaxPercentage(BigDecimal taxPercentage) { this.taxPercentage = taxPercentage; }
}
