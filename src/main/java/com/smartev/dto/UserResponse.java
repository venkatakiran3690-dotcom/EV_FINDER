package com.smartev.dto;

import com.smartev.entity.Role;
import com.smartev.entity.User;

import java.time.LocalDateTime;

public class UserResponse {

    private Long id;
    private String email;
    private String fullName;
    private String phone;
    private Role role;
    private Long companyId;
    private String companyName;
    private String status;
    private LocalDateTime createdAt;

    public UserResponse() {}

    public UserResponse(Long id, String email, String fullName, String phone, Role role,
                        Long companyId, String companyName, String status, LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.phone = phone;
        this.role = role;
        this.companyId = companyId;
        this.companyName = companyName;
        this.status = status;
        this.createdAt = createdAt;
    }

    public UserResponse(User user) {
        if (user != null) {
            this.id = user.getId();
            this.email = user.getEmail();
            this.fullName = user.getFullName();
            this.phone = user.getPhone();
            this.role = user.getRole();
            this.status = user.getStatus();
            this.createdAt = user.getCreatedAt();
            if (user.getCompany() != null) {
                try {
                    this.companyId = user.getCompany().getId();
                    this.companyName = user.getCompany().getName();
                } catch (Exception ignored) {
                    // Lazy proxy fallback
                }
            }
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
