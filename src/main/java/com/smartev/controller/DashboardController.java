package com.smartev.controller;

import com.smartev.dto.DashboardStatsResponse;
import com.smartev.entity.User;
import com.smartev.exception.ResourceNotFoundException;
import com.smartev.repository.UserRepository;
import com.smartev.security.UserPrincipal;
import com.smartev.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final UserRepository userRepository;

    public DashboardController(DashboardService dashboardService, UserRepository userRepository) {
        this.dashboardService = dashboardService;
        this.userRepository = userRepository;
    }

    @GetMapping("/driver")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<DashboardStatsResponse> getDriverDashboard(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        User driver = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(dashboardService.getDriverDashboard(driver));
    }

    @GetMapping("/company")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'PLATFORM_ADMIN')")
    public ResponseEntity<DashboardStatsResponse> getCompanyDashboard(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(dashboardService.getCompanyAdminDashboard(user));
    }

    @GetMapping("/manager")
    @PreAuthorize("hasAnyRole('STATION_MANAGER', 'COMPANY_ADMIN', 'PLATFORM_ADMIN')")
    public ResponseEntity<DashboardStatsResponse> getManagerDashboard(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(dashboardService.getCompanyAdminDashboard(user));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<DashboardStatsResponse> getPlatformAdminDashboard() {
        return ResponseEntity.ok(dashboardService.getPlatformAdminDashboard());
    }
}
