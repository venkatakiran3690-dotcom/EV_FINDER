package com.smartev.dto;

import jakarta.validation.constraints.NotBlank;

public class ComplaintUpdateRequest {

    @NotBlank(message = "Status is required (OPEN, IN_REVIEW, RESOLVED, CLOSED)")
    private String status;

    private String resolutionNotes;

    public ComplaintUpdateRequest() {}

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getResolutionNotes() { return resolutionNotes; }
    public void setResolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; }
}
