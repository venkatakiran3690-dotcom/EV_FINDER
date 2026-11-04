package com.smartev.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class SessionCompleteRequest {

    @Min(value = 10, message = "Final battery percentage must be at least 10%")
    @Max(value = 100, message = "Final battery percentage cannot exceed 100%")
    private Integer finalBatteryPct;

    public SessionCompleteRequest() {}

    public SessionCompleteRequest(Integer finalBatteryPct) {
        this.finalBatteryPct = finalBatteryPct;
    }

    public Integer getFinalBatteryPct() { return finalBatteryPct; }
    public void setFinalBatteryPct(Integer finalBatteryPct) { this.finalBatteryPct = finalBatteryPct; }
}
