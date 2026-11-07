package com.smartev.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "companies")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 150)
    private String name;

    @Column(name = "registration_number", unique = true, length = 100)
    private String registrationNumber;

    @Column(name = "contact_email", length = 120)
    private String contactEmail;

    @Column(name = "contact_phone", length = 30)
    private String contactPhone;

    @Column(length = 255)
    private String address;

    @Column(nullable = false, length = 30)
    private String status = "ACTIVE"; // ACTIVE, SUSPENDED, DEACTIVATED

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Company() {}

    public Company(String name, String registrationNumber, String contactEmail, String contactPhone, String address) {
        this.name = name;
        this.registrationNumber = registrationNumber;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        this.address = address;
        this.status = "ACTIVE";
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
