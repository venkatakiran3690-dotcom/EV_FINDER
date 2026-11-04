package com.smartev.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class MaintenanceRequest {

    @NotNull(message = "Station ID is required")
    private Long stationId;

    private Long chargerId;

    @NotBlank(message = "Issue description is required")
    private String issueDescription;

    private String priority = "MEDIUM"; // LOW, MEDIUM, HIGH, URGENT

    public MaintenanceRequest() {}

    public Long getStationId() { return stationId; }
    public void setStationId(Long stationId) { this.stationId = stationId; }

    public Long getChargerId() { return chargerId; }
    public void setChargerId(Long chargerId) { this.chargerId = chargerId; }

    public String getIssueDescription() { return issueDescription; }
    public void setIssueDescription(String issueDescription) { this.issueDescription = issueDescription; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
}
