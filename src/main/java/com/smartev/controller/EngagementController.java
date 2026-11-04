package com.smartev.controller;

import com.smartev.dto.*;
import com.smartev.entity.Notification;
import com.smartev.entity.User;
import com.smartev.exception.ResourceNotFoundException;
import com.smartev.repository.UserRepository;
import com.smartev.security.UserPrincipal;
import com.smartev.service.EngagementService;
import com.smartev.service.StationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class EngagementController {

    private final EngagementService engagementService;
    private final StationService stationService;
    private final UserRepository userRepository;

    public EngagementController(EngagementService engagementService,
                                StationService stationService,
                                UserRepository userRepository) {
        this.engagementService = engagementService;
        this.stationService = stationService;
        this.userRepository = userRepository;
    }

    // --- Reviews ---
    @PostMapping("/reviews")
    public ResponseEntity<ReviewResponse> addReview(
            @Valid @RequestBody ReviewRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        User driver = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(engagementService.addReview(request, driver));
    }

    @GetMapping("/reviews/station/{stationId}")
    public ResponseEntity<List<ReviewResponse>> getStationReviews(@PathVariable Long stationId) {
        return ResponseEntity.ok(engagementService.getReviewsForStation(stationId));
    }

    // --- Favorites ---
    @PostMapping("/favorites/toggle/{stationId}")
    public ResponseEntity<Map<String, Object>> toggleFavorite(
            @PathVariable Long stationId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        User driver = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        engagementService.toggleFavorite(stationId, driver);
        return ResponseEntity.ok(Map.of("success", true, "message", "Favorite updated."));
    }

    @GetMapping("/favorites")
    public ResponseEntity<List<StationResponse>> getFavorites(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        User driver = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(engagementService.getFavoriteStations(driver, stationService));
    }

    // --- Notifications ---
    @GetMapping("/notifications")
    public ResponseEntity<List<Notification>> getNotifications(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(engagementService.getNotificationsForUser(user));
    }

    @PutMapping("/notifications/{id}/read")
    public ResponseEntity<Map<String, Object>> markNotificationRead(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        engagementService.markNotificationRead(id, user);
        return ResponseEntity.ok(Map.of("success", true));
    }

    // --- Complaints ---
    @PostMapping("/complaints")
    public ResponseEntity<ComplaintResponse> fileComplaint(
            @Valid @RequestBody ComplaintRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        User driver = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(engagementService.fileComplaint(request, driver));
    }

    @GetMapping("/complaints")
    public ResponseEntity<List<ComplaintResponse>> getComplaints(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(engagementService.getComplaintsForUser(user));
    }

    @PutMapping("/complaints/{id}/status")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'STATION_MANAGER', 'PLATFORM_ADMIN')")
    public ResponseEntity<ComplaintResponse> updateComplaintStatus(
            @PathVariable Long id,
            @Valid @RequestBody ComplaintUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(engagementService.updateComplaintStatus(id, request, user));
    }

    // --- Maintenance ---
    @PostMapping("/maintenance")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'STATION_MANAGER', 'PLATFORM_ADMIN')")
    public ResponseEntity<MaintenanceResponse> createMaintenance(
            @Valid @RequestBody MaintenanceRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(engagementService.submitMaintenanceRequest(request, user));
    }

    @GetMapping("/maintenance")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'STATION_MANAGER', 'PLATFORM_ADMIN')")
    public ResponseEntity<List<MaintenanceResponse>> getMaintenance(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(engagementService.getMaintenanceRecords(user));
    }
}
