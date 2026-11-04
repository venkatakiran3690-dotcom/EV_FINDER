package com.smartev.controller;

import com.smartev.dto.RegisterRequest;
import com.smartev.entity.*;
import com.smartev.exception.BadRequestException;
import com.smartev.exception.ResourceNotFoundException;
import com.smartev.repository.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    public ResponseEntity<Company> createCompany(@RequestBody Map<String, String> payload) {
        String name = payload.get("name");
        String regNum = payload.get("registrationNumber");
        String email = payload.get("contactEmail");
        String phone = payload.get("contactPhone");
        String address = payload.get("address");

        if (name == null || name.trim().isEmpty()) {
            throw new BadRequestException("Company name is required.");
        }

        Company company = new Company(name, regNum, email, phone, address);
        return ResponseEntity.ok(companyRepository.save(company));
    }

    @PutMapping("/companies/{id}/status")
    public ResponseEntity<Company> updateCompanyStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {

        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with ID: " + id));

        String status = payload.get("status");
        if (status != null) {
            company.setStatus(status.toUpperCase());
        }

        return ResponseEntity.ok(companyRepository.save(company));
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getUsers(@RequestParam(required = false) Role role) {
        if (role != null) {
            return ResponseEntity.ok(userRepository.findByRole(role));
        }
        return ResponseEntity.ok(userRepository.findAll());
    }

    @PostMapping("/users")
    public ResponseEntity<Map<String, Object>> createPrivilegedUser(@RequestBody Map<String, String> payload) {
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

        userRepository.save(user);
        return ResponseEntity.ok(Map.of("success", true, "message", "User created with role " + role));
    }

    @GetMapping("/logs")
    public ResponseEntity<List<ActivityLog>> getLogs() {
        return ResponseEntity.ok(activityLogRepository.findTop50ByOrderByTimestampDesc());
    }
}
