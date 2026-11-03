package com.smartev.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chargers")
public class Charger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id", nullable = false)
    private Station station;

    @Column(name = "charger_identifier", nullable = false, length = 60)
    private String chargerIdentifier;

    @Column(name = "charger_type", nullable = false, length = 20)
    private String chargerType; // AC, DC

    @Column(name = "connector_type", nullable = false, length = 50)
    private String connectorType; // CCS2, Type 2, CHAdeMO, GB/T

    @Column(name = "power_rating_kw", nullable = false)
    private Double powerRatingKw;

    @Column(name = "operational_status", nullable = false, length = 30)
    private String operationalStatus = "OPERATIONAL"; // OPERATIONAL, OUT_OF_SERVICE

    @Column(name = "availability_status", nullable = false, length = 30)
    private String availabilityStatus = "AVAILABLE"; // AVAILABLE, OCCUPIED, RESERVED

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Charger() {}

    public Charger(Station station, String chargerIdentifier, String chargerType, String connectorType,
                   Double powerRatingKw, String operationalStatus, String availabilityStatus) {
        this.station = station;
        this.chargerIdentifier = chargerIdentifier;
        this.chargerType = chargerType;
        this.connectorType = connectorType;
        this.powerRatingKw = powerRatingKw;
        this.operationalStatus = operationalStatus;
        this.availabilityStatus = availabilityStatus;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Station getStation() { return station; }
    public void setStation(Station station) { this.station = station; }

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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
