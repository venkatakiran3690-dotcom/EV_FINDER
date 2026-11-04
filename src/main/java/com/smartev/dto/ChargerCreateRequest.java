package com.smartev.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class ChargerCreateRequest {

    @NotBlank(message = "Charger identifier is required")
    private String chargerIdentifier;

    @NotBlank(message = "Charger type (AC/DC) is required")
    private String chargerType;

    @NotBlank(message = "Connector type is required")
    private String connectorType;

    @NotNull(message = "Power rating is required")
    @Positive(message = "Power rating must be positive")
    private Double powerRatingKw;

    private String operationalStatus = "OPERATIONAL";

    public ChargerCreateRequest() {}

    public String getChargerIdentifier() { return chargerIdentifier; }
    public void setChargerIdentifier(String chargerIdentifier) { this.chargerIdentifier = chargerIdentifier; }

    public String getChargerType() { return chargerType; }
    public void setChargerType(String chargerType) { this.chargerType = chargerType; }

    public String getConnectorType() { return connectorType; }
    public void setConnectorType(String connectorType) { this.connectorType = connectorType; }

    public Double getPowerRatingKw() { return powerRatingKw; }
    public void setPowerRatingKw(Double powerRatingKw) { this.powerRatingKw = powerRatingKw; }

    public String getOperationalStatus() { return operationalStatus; }
    public void setOperationalStatus(String operationalStatus) { this.operationalStatus = operationalStatus; }
}
