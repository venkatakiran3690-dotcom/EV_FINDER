package com.smartev.controller;

import com.smartev.dto.SessionCompleteRequest;
import com.smartev.dto.SessionResponse;
import com.smartev.dto.SessionStartRequest;
import com.smartev.entity.User;
import com.smartev.exception.ResourceNotFoundException;
import com.smartev.repository.UserRepository;
import com.smartev.security.UserPrincipal;
import com.smartev.service.ChargingSessionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
public class ChargingSessionController {

    private final ChargingSessionService sessionService;
    private final UserRepository userRepository;

    public ChargingSessionController(ChargingSessionService sessionService, UserRepository userRepository) {
        this.sessionService = sessionService;
        this.userRepository = userRepository;
    }

    /**
     * Start a simulated charging session from a confirmed booking.
     */
    @PostMapping("/start")
    public ResponseEntity<SessionResponse> startSession(
            @Valid @RequestBody SessionStartRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        User driver = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(sessionService.startSession(request, driver));
    }

    /**
     * Get active charging session for logged-in driver.
     */
    @GetMapping("/active")
    public ResponseEntity<SessionResponse> getActiveSession(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        User driver = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        SessionResponse session = sessionService.getActiveSessionForDriver(driver);
        return session != null ? ResponseEntity.ok(session) : ResponseEntity.noContent().build();
    }

    /**
     * Get details of a charging session by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<SessionResponse> getSessionById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        User currentUser = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(sessionService.getSessionById(id, currentUser));
    }

    /**
     * Complete an active charging session and trigger billing generation.
     */
    @PostMapping("/{id}/complete")
    public ResponseEntity<SessionResponse> completeSession(
            @PathVariable Long id,
            @RequestBody(required = false) SessionCompleteRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        User driver = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(sessionService.completeSession(id, request, driver));
    }

    /**
     * List all charging sessions for authenticated user.
     */
    @GetMapping
    public ResponseEntity<List<SessionResponse>> getSessions(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        User currentUser = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(sessionService.getSessionsForUser(currentUser));
    }
}
