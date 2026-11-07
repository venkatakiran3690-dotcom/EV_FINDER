package com.smartev.controller;

import com.smartev.dto.ChargerCreateRequest;
import com.smartev.dto.ChargerResponse;
import com.smartev.dto.StationCreateRequest;
import com.smartev.dto.StationResponse;
import com.smartev.entity.User;
import com.smartev.exception.ResourceNotFoundException;
import com.smartev.repository.UserRepository;
import com.smartev.security.UserPrincipal;
import com.smartev.service.StationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stations")
public class StationController {

    private final StationService stationService;
    private final UserRepository userRepository;

    public StationController(StationService stationService, UserRepository userRepository) {
        this.stationService = stationService;
        this.userRepository = userRepository;
    }

    /**
     * Search and discover charging stations with filters (AC/DC, connector, power) and Haversine distance sorting.
     */
    @GetMapping
    public ResponseEntity<List<StationResponse>> getStations(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String chargerType,
            @RequestParam(required = false) String connectorType,
            @RequestParam(required = false) Double minPower,
            @RequestParam(required = false) Double driverLat,
            @RequestParam(required = false) Double driverLon) {

        List<StationResponse> stations = stationService.getAllStations(
                query, chargerType, connectorType, minPower, driverLat, driverLon);
        return ResponseEntity.ok(stations);
    }

    /**
     * Get stations explicitly assigned to the authenticated station manager.
     */
    @GetMapping("/manager/assigned")
    @PreAuthorize("hasAnyRole('STATION_MANAGER', 'COMPANY_ADMIN', 'PLATFORM_ADMIN')")
    public ResponseEntity<List<StationResponse>> getAssignedStations(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        User currentUser = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(stationService.getAssignedStationsForManager(currentUser));
    }

    /**
     * Get station details by ID with dynamic charger status, occupancy %, and estimated wait time.
     */
    @GetMapping("/{id}")
    public ResponseEntity<StationResponse> getStationById(
            @PathVariable Long id,
            @RequestParam(required = false) Double driverLat,
            @RequestParam(required = false) Double driverLon) {

        StationResponse station = stationService.getStationById(id, driverLat, driverLon);
        return ResponseEntity.ok(station);
    }

    /**
     * Get chargers for a specific station.
     */
    @GetMapping("/{id}/chargers")
    public ResponseEntity<List<ChargerResponse>> getChargers(@PathVariable Long id) {
        return ResponseEntity.ok(stationService.getChargersByStation(id));
    }

    /**
     * Create a new charging station (Company Admin or Platform Admin).
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'PLATFORM_ADMIN')")
    public ResponseEntity<StationResponse> createStation(
            @Valid @RequestBody StationCreateRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        User currentUser = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(stationService.createStation(request, currentUser));
    }

    /**
     * Register a new charger to a station.
     */
    @PostMapping("/{id}/chargers")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'STATION_MANAGER', 'PLATFORM_ADMIN')")
    public ResponseEntity<ChargerResponse> addCharger(
            @PathVariable Long id,
            @Valid @RequestBody ChargerCreateRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        User currentUser = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(stationService.addChargerToStation(id, request, currentUser));
    }

    /**
     * Update operational status of a charger (e.g. OPERATIONAL vs OUT_OF_SERVICE).
     */
    @PatchMapping("/chargers/{id}/status")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'STATION_MANAGER', 'PLATFORM_ADMIN')")
    public ResponseEntity<ChargerResponse> updateChargerStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> statusUpdate,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        User currentUser = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String operationalStatus = statusUpdate.get("operationalStatus");
        String availabilityStatus = statusUpdate.get("availabilityStatus");

        return ResponseEntity.ok(stationService.updateChargerStatus(id, operationalStatus, availabilityStatus, currentUser));
    }

    /**
     * Update/Edit station details (Company Admin or Platform Admin).
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'PLATFORM_ADMIN')")
    public ResponseEntity<StationResponse> updateStation(
            @PathVariable Long id,
            @Valid @RequestBody StationCreateRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        User currentUser = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(stationService.updateStation(id, request, currentUser));
    }

    /**
     * Delete a charging station and all associated chargers (Company Admin or Platform Admin).
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'PLATFORM_ADMIN')")
    public ResponseEntity<Map<String, String>> deleteStation(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        User currentUser = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        stationService.deleteStation(id, currentUser);
        return ResponseEntity.ok(Map.of("message", "Station deleted successfully"));
    }

    /**
     * Delete an individual charger post from a station.
     */
    @DeleteMapping("/chargers/{chargerId}")
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'PLATFORM_ADMIN')")
    public ResponseEntity<Map<String, String>> deleteCharger(
            @PathVariable Long chargerId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        User currentUser = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        stationService.deleteCharger(chargerId, currentUser);
        return ResponseEntity.ok(Map.of("message", "Charger deleted successfully"));
    }
}
