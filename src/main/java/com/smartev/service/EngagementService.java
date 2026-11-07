package com.smartev.service;

import com.smartev.dto.*;
import com.smartev.entity.*;
import com.smartev.exception.BadRequestException;
import com.smartev.exception.ResourceNotFoundException;
import com.smartev.exception.UnauthorizedException;
import com.smartev.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EngagementService {

    private final ReviewRepository reviewRepository;
    private final FavoriteRepository favoriteRepository;
    private final NotificationRepository notificationRepository;
    private final ComplaintRepository complaintRepository;
    private final MaintenanceRecordRepository maintenanceRepository;
    private final StationRepository stationRepository;
    private final ChargerRepository chargerRepository;
    private final CompanyRepository companyRepository;
    private final StationManagerAssignmentRepository assignmentRepository;

    public EngagementService(ReviewRepository reviewRepository,
                             FavoriteRepository favoriteRepository,
                             NotificationRepository notificationRepository,
                             ComplaintRepository complaintRepository,
                             MaintenanceRecordRepository maintenanceRepository,
                             StationRepository stationRepository,
                             ChargerRepository chargerRepository,
                             CompanyRepository companyRepository) {
        this(reviewRepository, favoriteRepository, notificationRepository, complaintRepository,
             maintenanceRepository, stationRepository, chargerRepository, companyRepository, null);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public EngagementService(ReviewRepository reviewRepository,
                             FavoriteRepository favoriteRepository,
                             NotificationRepository notificationRepository,
                             ComplaintRepository complaintRepository,
                             MaintenanceRecordRepository maintenanceRepository,
                             StationRepository stationRepository,
                             ChargerRepository chargerRepository,
                             CompanyRepository companyRepository,
                             StationManagerAssignmentRepository assignmentRepository) {
        this.reviewRepository = reviewRepository;
        this.favoriteRepository = favoriteRepository;
        this.notificationRepository = notificationRepository;
        this.complaintRepository = complaintRepository;
        this.maintenanceRepository = maintenanceRepository;
        this.stationRepository = stationRepository;
        this.chargerRepository = chargerRepository;
        this.companyRepository = companyRepository;
        this.assignmentRepository = assignmentRepository;
    }

    // --- Reviews ---
    @Transactional
    public ReviewResponse addReview(ReviewRequest request, User driver) {
        Station station = stationRepository.findById(request.getStationId())
                .orElseThrow(() -> new ResourceNotFoundException("Station not found with ID: " + request.getStationId()));

        Review review = new Review(station, driver, request.getRating(), request.getComment());
        Review saved = reviewRepository.save(review);
        return mapReviewToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsForStation(Long stationId) {
        return reviewRepository.findByStationIdOrderByCreatedAtDesc(stationId).stream()
                .map(this::mapReviewToResponse)
                .collect(Collectors.toList());
    }

    // --- Favorites ---
    @Transactional
    public void toggleFavorite(Long stationId, User driver) {
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new ResourceNotFoundException("Station not found with ID: " + stationId));

        if (favoriteRepository.existsByDriverIdAndStationId(driver.getId(), stationId)) {
            favoriteRepository.deleteByDriverIdAndStationId(driver.getId(), stationId);
        } else {
            Favorite favorite = new Favorite(driver, station);
            favoriteRepository.save(favorite);
        }
    }

    @Transactional(readOnly = true)
    public List<StationResponse> getFavoriteStations(User driver, StationService stationService) {
        List<Favorite> favorites = favoriteRepository.findByDriverIdOrderByCreatedAtDesc(driver.getId());
        return favorites.stream()
                .map(f -> stationService.buildStationResponse(f.getStation(), null, null))
                .collect(Collectors.toList());
    }

    // --- Notifications ---
    @Transactional(readOnly = true)
    public List<Notification> getNotificationsForUser(User user) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
    }

    @Transactional
    public void markNotificationRead(Long notificationId, User user) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with ID: " + notificationId));
        if (notification.getUser().getId().equals(user.getId())) {
            notification.setIsRead(true);
            notificationRepository.save(notification);
        }
    }

    // --- Complaints ---
    @Transactional
    public ComplaintResponse fileComplaint(ComplaintRequest request, User driver) {
        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with ID: " + request.getCompanyId()));

        Station station = null;
        if (request.getStationId() != null) {
            station = stationRepository.findById(request.getStationId()).orElse(null);
        }

        Complaint complaint = new Complaint(driver, company, station, request.getSubject(), request.getDescription());
        Complaint saved = complaintRepository.save(complaint);
        return mapComplaintToResponse(saved);
    }

    @Transactional
    public ComplaintResponse updateComplaintStatus(Long complaintId, ComplaintUpdateRequest request, User user) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with ID: " + complaintId));

        if (user.getRole() == Role.COMPANY_ADMIN) {
            if (user.getCompany() == null || !user.getCompany().getId().equals(complaint.getCompany().getId())) {
                throw new UnauthorizedException("Unauthorized: Complaint belongs to another company.");
            }
        }

        complaint.setStatus(request.getStatus().toUpperCase());
        if (request.getResolutionNotes() != null) {
            complaint.setResolutionNotes(request.getResolutionNotes());
        }
        complaint.setUpdatedAt(LocalDateTime.now());

        Complaint saved = complaintRepository.save(complaint);
        return mapComplaintToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ComplaintResponse> getComplaintsForUser(User user) {
        if (user.getRole() == Role.DRIVER) {
            return complaintRepository.findByDriverIdOrderByCreatedAtDesc(user.getId()).stream()
                    .map(this::mapComplaintToResponse)
                    .collect(Collectors.toList());
        } else if (user.getRole() == Role.COMPANY_ADMIN) {
            if (user.getCompany() == null) return List.of();
            return complaintRepository.findByCompanyIdOrderByCreatedAtDesc(user.getCompany().getId()).stream()
                    .map(this::mapComplaintToResponse)
                    .collect(Collectors.toList());
        } else {
            return complaintRepository.findAll().stream()
                    .map(this::mapComplaintToResponse)
                    .collect(Collectors.toList());
        }
    }

    // --- Maintenance ---
    @Transactional
    public MaintenanceResponse submitMaintenanceRequest(MaintenanceRequest request, User user) {
        Station station = stationRepository.findById(request.getStationId())
                .orElseThrow(() -> new ResourceNotFoundException("Station not found with ID: " + request.getStationId()));

        if (user.getRole() == Role.STATION_MANAGER) {
            if (user.getCompany() == null || !user.getCompany().getId().equals(station.getCompany().getId())) {
                throw new UnauthorizedException("Unauthorized: Station belongs to a different charging company.");
            }
            boolean isAssigned = assignmentRepository != null && assignmentRepository.existsByUserIdAndStationId(user.getId(), station.getId());
            if (!isAssigned) {
                throw new UnauthorizedException("Unauthorized: Cannot manage maintenance for an unassigned station.");
            }
        } else if (user.getRole() == Role.COMPANY_ADMIN) {
            if (user.getCompany() == null || !user.getCompany().getId().equals(station.getCompany().getId())) {
                throw new UnauthorizedException("Unauthorized: Station belongs to a different charging company.");
            }
        }

        Charger charger = null;
        if (request.getChargerId() != null) {
            charger = chargerRepository.findById(request.getChargerId()).orElse(null);
            if (charger != null) {
                charger.setOperationalStatus("OUT_OF_SERVICE");
                chargerRepository.save(charger);
            }
        }

        MaintenanceRecord record = new MaintenanceRecord(
                station,
                charger,
                station.getCompany(),
                user,
                request.getIssueDescription(),
                request.getPriority() != null ? request.getPriority() : "MEDIUM"
        );

        MaintenanceRecord saved = maintenanceRepository.save(record);
        return mapMaintenanceToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<MaintenanceResponse> getMaintenanceRecords(User user) {
        if (user.getRole() == Role.COMPANY_ADMIN) {
            if (user.getCompany() == null) return List.of();
            return maintenanceRepository.findByCompanyIdOrderByCreatedAtDesc(user.getCompany().getId()).stream()
                    .map(this::mapMaintenanceToResponse)
                    .collect(Collectors.toList());
        } else if (user.getRole() == Role.STATION_MANAGER) {
            List<Long> assignedIds = assignmentRepository != null
                    ? assignmentRepository.findByUserId(user.getId()).stream().map(a -> a.getStation().getId()).toList()
                    : List.of();
            if (assignedIds.isEmpty()) return List.of();
            return maintenanceRepository.findByStationIdInOrderByCreatedAtDesc(assignedIds).stream()
                    .map(this::mapMaintenanceToResponse)
                    .collect(Collectors.toList());
        } else {
            return maintenanceRepository.findAll().stream()
                    .map(this::mapMaintenanceToResponse)
                    .collect(Collectors.toList());
        }
    }

    private ReviewResponse mapReviewToResponse(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getStation().getId(),
                review.getStation().getName(),
                review.getDriver().getId(),
                review.getDriver().getFullName(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt()
        );
    }

    private ComplaintResponse mapComplaintToResponse(Complaint c) {
        ComplaintResponse res = new ComplaintResponse();
        res.setId(c.getId());
        res.setDriverId(c.getDriver().getId());
        res.setDriverName(c.getDriver().getFullName());
        res.setDriverEmail(c.getDriver().getEmail());
        res.setCompanyId(c.getCompany().getId());
        res.setCompanyName(c.getCompany().getName());
        if (c.getStation() != null) {
            res.setStationId(c.getStation().getId());
            res.setStationName(c.getStation().getName());
        }
        res.setSubject(c.getSubject());
        res.setDescription(c.getDescription());
        res.setStatus(c.getStatus());
        res.setResolutionNotes(c.getResolutionNotes());
        res.setCreatedAt(c.getCreatedAt());
        res.setUpdatedAt(c.getUpdatedAt());
        return res;
    }

    private MaintenanceResponse mapMaintenanceToResponse(MaintenanceRecord m) {
        MaintenanceResponse res = new MaintenanceResponse();
        res.setId(m.getId());
        res.setStationId(m.getStation().getId());
        res.setStationName(m.getStation().getName());
        if (m.getCharger() != null) {
            res.setChargerId(m.getCharger().getId());
            res.setChargerIdentifier(m.getCharger().getChargerIdentifier());
        }
        res.setCompanyId(m.getCompany().getId());
        if (m.getReportedByUser() != null) {
            res.setReportedByName(m.getReportedByUser().getFullName());
        }
        res.setIssueDescription(m.getIssueDescription());
        res.setPriority(m.getPriority());
        res.setStatus(m.getStatus());
        res.setScheduledDate(m.getScheduledDate());
        res.setCompletedDate(m.getCompletedDate());
        res.setCreatedAt(m.getCreatedAt());
        return res;
    }
}
