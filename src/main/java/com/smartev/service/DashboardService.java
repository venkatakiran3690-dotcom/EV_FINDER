package com.smartev.service;

import com.smartev.dto.DashboardStatsResponse;
import com.smartev.entity.Role;
import com.smartev.entity.User;
import com.smartev.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
public class DashboardService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final StationRepository stationRepository;
    private final ChargerRepository chargerRepository;
    private final BookingRepository bookingRepository;
    private final ChargingSessionRepository sessionRepository;
    private final BillRepository billRepository;
    private final ComplaintRepository complaintRepository;
    private final MaintenanceRecordRepository maintenanceRepository;
    private final NotificationRepository notificationRepository;

    public DashboardService(UserRepository userRepository,
                            CompanyRepository companyRepository,
                            StationRepository stationRepository,
                            ChargerRepository chargerRepository,
                            BookingRepository bookingRepository,
                            ChargingSessionRepository sessionRepository,
                            BillRepository billRepository,
                            ComplaintRepository complaintRepository,
                            MaintenanceRecordRepository maintenanceRepository,
                            NotificationRepository notificationRepository) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.stationRepository = stationRepository;
        this.chargerRepository = chargerRepository;
        this.bookingRepository = bookingRepository;
        this.sessionRepository = sessionRepository;
        this.billRepository = billRepository;
        this.complaintRepository = complaintRepository;
        this.maintenanceRepository = maintenanceRepository;
        this.notificationRepository = notificationRepository;
    }

    @Transactional(readOnly = true)
    public DashboardStatsResponse getPlatformAdminDashboard() {
        DashboardStatsResponse stats = new DashboardStatsResponse();
        stats.setRole(Role.PLATFORM_ADMIN.name());
        stats.setTotalDrivers(userRepository.countByRole(Role.DRIVER));
        stats.setTotalCompanies(companyRepository.count());
        stats.setTotalStations(stationRepository.count());
        stats.setTotalChargers(chargerRepository.count());

        stats.setAvailableChargers(chargerRepository.countByStationIdAndAvailabilityStatus(null, "AVAILABLE"));
        stats.setTotalBookings(bookingRepository.count());
        stats.setActiveSessions(sessionRepository.countByStatus("ACTIVE"));
        stats.setCompletedSessions(sessionRepository.countByStatus("COMPLETED"));
        stats.setTotalRevenue(billRepository.calculatePlatformTotalRevenue());

        // Environmental calculation (based on CEA India grid emission baseline 0.82 kg CO2/kWh)
        Double totalEnergy = sessionRepository.findAll().stream()
                .mapToDouble(s -> s.getEnergyConsumedKwh() != null ? s.getEnergyConsumedKwh() : 0.0)
                .sum();
        stats.setTotalEnergyKwh(BigDecimal.valueOf(totalEnergy).setScale(1, RoundingMode.HALF_UP).doubleValue());
        stats.setEstimatedCo2SavedKg(BigDecimal.valueOf(totalEnergy * 0.82).setScale(1, RoundingMode.HALF_UP).doubleValue());

        stats.setPendingComplaints(complaintRepository.findByStatus("OPEN").size() + (long) complaintRepository.findByStatus("IN_REVIEW").size());
        stats.setPendingMaintenance(maintenanceRepository.findByStatus("PENDING").size() + (long) maintenanceRepository.findByStatus("IN_PROGRESS").size());

        return stats;
    }

    @Transactional(readOnly = true)
    public DashboardStatsResponse getCompanyAdminDashboard(User user) {
        DashboardStatsResponse stats = new DashboardStatsResponse();
        stats.setRole(Role.COMPANY_ADMIN.name());
        if (user.getCompany() == null) return stats;

        Long companyId = user.getCompany().getId();
        stats.setTotalStations(stationRepository.countByCompanyId(companyId));
        stats.setTotalChargers(chargerRepository.countByStationCompanyId(companyId));
        stats.setAvailableChargers(chargerRepository.countByStationCompanyIdAndAvailabilityStatus(companyId, "AVAILABLE"));
        stats.setOccupiedChargers(chargerRepository.countByStationCompanyIdAndAvailabilityStatus(companyId, "OCCUPIED"));
        stats.setOutOfServiceChargers(chargerRepository.countByStationCompanyIdAndOperationalStatus(companyId, "OUT_OF_SERVICE"));

        stats.setTotalBookings(bookingRepository.countByStationCompanyId(companyId));

        LocalDateTime dayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime dayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        stats.setTodayBookings((long) bookingRepository.findCompanyBookingsForDay(companyId, dayStart, dayEnd).size());

        stats.setActiveSessions(sessionRepository.countByCompanyIdAndStatus(companyId, "ACTIVE"));
        stats.setCompletedSessions(sessionRepository.countByCompanyIdAndStatus(companyId, "COMPLETED"));
        stats.setTotalRevenue(billRepository.calculateCompanyTotalRevenue(companyId));

        stats.setPendingComplaints(complaintRepository.countByCompanyIdAndStatus(companyId, "OPEN")
                + complaintRepository.countByCompanyIdAndStatus(companyId, "IN_REVIEW"));
        stats.setPendingMaintenance(maintenanceRepository.countByCompanyIdAndStatus(companyId, "PENDING")
                + maintenanceRepository.countByCompanyIdAndStatus(companyId, "IN_PROGRESS"));

        return stats;
    }

    @Transactional(readOnly = true)
    public DashboardStatsResponse getDriverDashboard(User driver) {
        DashboardStatsResponse stats = new DashboardStatsResponse();
        stats.setRole(Role.DRIVER.name());

        stats.setTotalBookings((long) bookingRepository.findByDriverIdOrderByCreatedAtDesc(driver.getId()).size());
        stats.setActiveSessions(sessionRepository.findFirstByDriverIdAndStatus(driver.getId(), "ACTIVE").isPresent() ? 1L : 0L);
        stats.setCompletedSessions((long) sessionRepository.findByDriverIdOrderByCreatedAtDesc(driver.getId()).stream()
                .filter(s -> "COMPLETED".equalsIgnoreCase(s.getStatus())).count());

        Double totalEnergy = billRepository.calculateDriverTotalEnergyConsumed(driver.getId());
        stats.setTotalEnergyKwh(BigDecimal.valueOf(totalEnergy != null ? totalEnergy : 0.0).setScale(1, RoundingMode.HALF_UP).doubleValue());

        // Environmental Benefit: EV charging vs Internal Combustion Engine baseline (0.82 kg CO2 saved per kWh)
        stats.setEstimatedCo2SavedKg(BigDecimal.valueOf(stats.getTotalEnergyKwh() * 0.82).setScale(1, RoundingMode.HALF_UP).doubleValue());

        BigDecimal expenditure = billRepository.calculateDriverTotalExpenditure(driver.getId());
        stats.setTotalRevenue(expenditure != null ? expenditure : BigDecimal.ZERO);

        stats.setUnreadNotifications(notificationRepository.countByUserIdAndIsReadFalse(driver.getId()));

        return stats;
    }
}
