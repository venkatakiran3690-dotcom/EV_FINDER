package com.smartev.controller;

import com.smartev.dto.RegisterRequest;
import com.smartev.dto.UserResponse;
import com.smartev.entity.*;
import com.smartev.exception.BadRequestException;
import com.smartev.exception.ResourceNotFoundException;
import com.smartev.repository.*;
import com.smartev.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('PLATFORM_ADMIN')")
public class PlatformAdminController {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final ActivityLogRepository activityLogRepository;
    private final PasswordEncoder passwordEncoder;

    public PlatformAdminController(CompanyRepository companyRepository,
                                   UserRepository userRepository,
                                   ActivityLogRepository activityLogRepository,
                                   PasswordEncoder passwordEncoder) {
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.activityLogRepository = activityLogRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/companies")
    public ResponseEntity<List<Company>> getCompanies() {
        return ResponseEntity.ok(companyRepository.findAll());
    }

    @PostMapping("/companies")
    public ResponseEntity<Company> createCompany(
            @RequestBody Map<String, String> payload,
            @AuthenticationPrincipal UserPrincipal admin) {
        String name = payload.get("name");
        String regNum = payload.get("registrationNumber");
        String email = payload.get("contactEmail");
        String phone = payload.get("contactPhone");
        String address = payload.get("address");

        if (name == null || name.trim().isEmpty()) {
            throw new BadRequestException("Company name is required.");
        }

        Company company = new Company(name, regNum, email, phone, address);
        Company saved = companyRepository.save(company);

        // Audit Log
        String actorEmail = admin != null ? admin.getUsername() : "admin@smartev.com";
        String actorRole = admin != null ? admin.getRole().name() : "PLATFORM_ADMIN";
        activityLogRepository.save(new ActivityLog(
                admin != null ? admin.getId() : 1L,
                actorEmail,
                actorRole,
                "CPO Onboarded: " + saved.getName(),
                "Registered company with registration " + saved.getRegistrationNumber(),
                "127.0.0.1"
        ));

        return ResponseEntity.ok(saved);
    }

    @PutMapping("/companies/{id}/status")
    public ResponseEntity<Company> updateCompanyStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload,
            @AuthenticationPrincipal UserPrincipal admin) {

        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with ID: " + id));

        String status = payload.get("status");
        if (status != null) {
            company.setStatus(status.toUpperCase());
        }

        Company updated = companyRepository.save(company);

        // Audit Log
        String actorEmail = admin != null ? admin.getUsername() : "admin@smartev.com";
        String actorRole = admin != null ? admin.getRole().name() : "PLATFORM_ADMIN";
        activityLogRepository.save(new ActivityLog(
                admin != null ? admin.getId() : 1L,
                actorEmail,
                actorRole,
                "CPO Status Updated: " + updated.getName(),
                "Updated status to " + updated.getStatus(),
                "127.0.0.1"
        ));

        return ResponseEntity.ok(updated);
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getUsers(@RequestParam(required = false) Role role) {
        List<User> users = (role != null)
                ? userRepository.findByRole(role)
                : userRepository.findAll();
        List<UserResponse> dtos = users.stream().map(UserResponse::new).toList();
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/users")
    public ResponseEntity<Map<String, Object>> createPrivilegedUser(
            @RequestBody Map<String, String> payload,
            @AuthenticationPrincipal UserPrincipal admin) {
        String email = payload.get("email");
        String password = payload.get("password");
        String fullName = payload.get("fullName");
        String phone = payload.get("phone");
        String roleStr = payload.get("role");
        String companyIdStr = payload.get("companyId");

        if (email == null || password == null || fullName == null || roleStr == null) {
            throw new BadRequestException("Email, password, fullName, and role are required.");
        }

        if (userRepository.existsByEmail(email.toLowerCase().trim())) {
            throw new BadRequestException("User with this email already exists.");
        }

        Role role = Role.valueOf(roleStr.toUpperCase());
        Company company = null;
        if (companyIdStr != null && !companyIdStr.isEmpty()) {
            company = companyRepository.findById(Long.parseLong(companyIdStr)).orElse(null);
        }

        User user = new User(
                email.toLowerCase().trim(),
                passwordEncoder.encode(password),
                fullName.trim(),
                phone,
                role,
                company
        );

        User saved = userRepository.save(user);

        // Audit Log
        String actorEmail = admin != null ? admin.getUsername() : "admin@smartev.com";
        String actorRole = admin != null ? admin.getRole().name() : "PLATFORM_ADMIN";
        activityLogRepository.save(new ActivityLog(
                admin != null ? admin.getId() : 1L,
                actorEmail,
                actorRole,
                "Privileged User Created: " + saved.getEmail(),
                "Created account for " + saved.getFullName() + " with role " + saved.getRole(),
                "127.0.0.1"
        ));

        return ResponseEntity.ok(Map.of("success", true, "message", "User created with role " + role));
    }

    @GetMapping("/logs")
    public ResponseEntity<List<ActivityLog>> getLogs() {
        return ResponseEntity.ok(activityLogRepository.findTop50ByOrderByTimestampDesc());
    }
}
