package com.smartev.dto;

public class ChargerResponse {

    private Long id;
    private Long stationId;
    private String chargerIdentifier;
    private String chargerType;
    private String connectorType;
    private Double powerRatingKw;
    private String operationalStatus;
    private String availabilityStatus;

    public ChargerResponse() {}

    public ChargerResponse(Long id, Long stationId, String chargerIdentifier, String chargerType,
                           String connectorType, Double powerRatingKw, String operationalStatus,
                           String availabilityStatus) {
        this.id = id;
        this.stationId = stationId;
        this.chargerIdentifier = chargerIdentifier;
        this.chargerType = chargerType;
        this.connectorType = connectorType;
        this.powerRatingKw = powerRatingKw;
        this.operationalStatus = operationalStatus;
        this.availabilityStatus = availabilityStatus;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getStationId() { return stationId; }
    public void setStationId(Long stationId) { this.stationId = stationId; }

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

    public String getAvailabilityStatus() { return availabilityStatus; }
    public void setAvailabilityStatus(String availabilityStatus) { this.availabilityStatus = availabilityStatus; }
}
