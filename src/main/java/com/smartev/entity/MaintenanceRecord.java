package com.smartev.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "maintenance_records")
public class MaintenanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id", nullable = false)
    private Station station;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "charger_id")
    private Charger charger;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_by_user_id")
    private User reportedByUser;

    @Column(name = "issue_description", nullable = false, length = 1500)
    private String issueDescription;

    @Column(nullable = false, length = 30)
    private String priority = "MEDIUM"; // LOW, MEDIUM, HIGH, URGENT

    @Column(nullable = false, length = 30)
    private String status = "PENDING"; // PENDING, IN_PROGRESS, COMPLETED

    @Column(name = "scheduled_date")
    private LocalDateTime scheduledDate;

    @Column(name = "completed_date")
    private LocalDateTime completedDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public MaintenanceRecord() {}

    public MaintenanceRecord(Station station, Charger charger, Company company, User reportedByUser,
                             String issueDescription, String priority) {
        this.station = station;
        this.charger = charger;
        this.company = company;
        this.reportedByUser = reportedByUser;
        this.issueDescription = issueDescription;
        this.priority = priority;
        this.status = "PENDING";
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Station getStation() { return station; }
    public void setStation(Station station) { this.station = station; }

    public Charger getCharger() { return charger; }
    public void setCharger(Charger charger) { this.charger = charger; }

    public Company getCompany() { return company; }
    public void setCompany(Company company) { this.company = company; }

    public User getReportedByUser() { return reportedByUser; }
    public void setReportedByUser(User reportedByUser) { this.reportedByUser = reportedByUser; }

    public String getIssueDescription() { return issueDescription; }
    public void setIssueDescription(String issueDescription) { this.issueDescription = issueDescription; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(LocalDateTime scheduledDate) { this.scheduledDate = scheduledDate; }

    public LocalDateTime getCompletedDate() { return completedDate; }
    public void setCompletedDate(LocalDateTime completedDate) { this.completedDate = completedDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
