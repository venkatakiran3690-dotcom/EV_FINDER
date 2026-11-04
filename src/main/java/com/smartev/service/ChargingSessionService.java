package com.smartev.service;

import com.smartev.dto.SessionCompleteRequest;
import com.smartev.dto.SessionResponse;
import com.smartev.dto.SessionStartRequest;
import com.smartev.entity.*;
import com.smartev.exception.BadRequestException;
import com.smartev.exception.ResourceNotFoundException;
import com.smartev.exception.UnauthorizedException;
import com.smartev.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ChargingSessionService {

    private final ChargingSessionRepository sessionRepository;
    private final BookingRepository bookingRepository;
    private final ChargerRepository chargerRepository;
    private final BillingService billingService;
    private final NotificationRepository notificationRepository;

    public ChargingSessionService(ChargingSessionRepository sessionRepository,
                                  BookingRepository bookingRepository,
                                  ChargerRepository chargerRepository,
                                  BillingService billingService,
                                  NotificationRepository notificationRepository) {
        this.sessionRepository = sessionRepository;
        this.bookingRepository = bookingRepository;
        this.chargerRepository = chargerRepository;
        this.billingService = billingService;
        this.notificationRepository = notificationRepository;
    }

    /**
     * Starts a simulated EV charging session linked to a confirmed slot booking.
     */
    @Transactional
    public SessionResponse startSession(SessionStartRequest request, User driver) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + request.getBookingId()));

        if (!booking.getDriver().getId().equals(driver.getId()) && driver.getRole() != Role.PLATFORM_ADMIN) {
            throw new UnauthorizedException("Unauthorized: You do not own this booking.");
        }

        if (!"CONFIRMED".equalsIgnoreCase(booking.getStatus())) {
            throw new BadRequestException("Cannot start session: Booking status is '" + booking.getStatus() + "'. Only CONFIRMED bookings can start charging.");
        }

        // Check if session already exists for this booking
        if (sessionRepository.findByBookingId(booking.getId()).isPresent()) {
            throw new BadRequestException("A charging session has already been created for this booking.");
        }

        Charger charger = booking.getCharger();
        if ("OUT_OF_SERVICE".equalsIgnoreCase(charger.getOperationalStatus())) {
            throw new BadRequestException("Charger is currently OUT OF SERVICE.");
        }

        String sessionCode = "SESS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Integer initialBatteryPct = request.getInitialBatteryPct() != null ? request.getInitialBatteryPct() : 20;

        ChargingSession session = new ChargingSession(
                booking,
                charger,
                driver,
                sessionCode,
                LocalDateTime.now(),
                initialBatteryPct
        );

        ChargingSession saved = sessionRepository.save(session);

        // Update booking and charger statuses
        booking.setStatus("IN_PROGRESS");
        bookingRepository.save(booking);

        charger.setAvailabilityStatus("OCCUPIED");
        chargerRepository.save(charger);

        // Notification
        Notification notification = new Notification(
                driver,
                "Charging Started: " + sessionCode,
                String.format("Session active at %s (%s). Starting battery: %d%%.",
                        booking.getStation().getName(), charger.getChargerIdentifier(), initialBatteryPct),
                "STATUS_UPDATE"
        );
        notificationRepository.save(notification);

        return mapSessionToResponse(saved);
    }

    /**
     * Completes an active charging session with deterministic kWh consumption and triggers billing.
     */
    @Transactional
    public SessionResponse completeSession(Long sessionId, SessionCompleteRequest request, User driver) {
        ChargingSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Charging session not found with ID: " + sessionId));

        if (!session.getDriver().getId().equals(driver.getId()) && driver.getRole() != Role.PLATFORM_ADMIN) {
            throw new UnauthorizedException("Unauthorized: You do not own this charging session.");
        }

        if (!"ACTIVE".equalsIgnoreCase(session.getStatus())) {
            throw new BadRequestException("Session is already " + session.getStatus() + ".");
        }

        LocalDateTime endTime = LocalDateTime.now();
        session.setEndTime(endTime);

        long elapsedMinutes = Duration.between(session.getStartTime(), endTime).toMinutes();
        if (elapsedMinutes < 1) {
            // For instantaneous academic demo completions, treat as a nominal 35-minute demo session
            elapsedMinutes = 35;
        }

        Charger charger = session.getCharger();
        Vehicle vehicle = session.getBooking().getVehicle();

        int finalBatteryPct;
        if (request != null && request.getFinalBatteryPct() != null) {
            finalBatteryPct = Math.max(session.getInitialBatteryPct(), request.getFinalBatteryPct());
        } else {
            // Deterministic battery calculation: +1.5% per minute up to 85%
            finalBatteryPct = Math.min(85, (int) (session.getInitialBatteryPct() + (elapsedMinutes * 1.5)));
        }
        session.setCurrentBatteryPct(finalBatteryPct);

        // Deterministic Energy Calculation (kWh):
        // Energy (kWh) = Charger Power (kW) * (duration in hours) * efficiency (0.92)
        double powerRating = charger.getPowerRatingKw() != null ? charger.getPowerRatingKw() : 50.0;
        double rawKwh = (powerRating * (elapsedMinutes / 60.0)) * 0.92;

        // If vehicle has known battery capacity, cap energy to battery capacity difference
        if (vehicle != null && vehicle.getBatteryCapacityKwh() != null && vehicle.getBatteryCapacityKwh() > 0) {
            double deltaPct = (finalBatteryPct - session.getInitialBatteryPct()) / 100.0;
            double vehicleRequiredKwh = vehicle.getBatteryCapacityKwh() * deltaPct;
            if (vehicleRequiredKwh > 0) {
                rawKwh = vehicleRequiredKwh;
            }
        }

        // Clamp minimum realistic kWh
        rawKwh = Math.max(1.5, rawKwh);

        double energyConsumedKwh = BigDecimal.valueOf(rawKwh).setScale(2, RoundingMode.HALF_UP).doubleValue();
        session.setEnergyConsumedKwh(energyConsumedKwh);
        session.setStatus("COMPLETED");

        ChargingSession completedSession = sessionRepository.save(session);

        // Complete booking and free up charger
        Booking booking = session.getBooking();
        booking.setStatus("COMPLETED");
        bookingRepository.save(booking);

        charger.setAvailabilityStatus("AVAILABLE");
        chargerRepository.save(charger);

        // Automatically Generate Bill & Invoice
        billingService.generateBillForSession(completedSession);

        return mapSessionToResponse(completedSession);
    }

    @Transactional(readOnly = true)
    public SessionResponse getSessionById(Long sessionId, User currentUser) {
        ChargingSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Charging session not found with ID: " + sessionId));

        validateSessionAccess(session, currentUser);
        return mapSessionToResponse(session);
    }

    @Transactional(readOnly = true)
    public SessionResponse getActiveSessionForDriver(User driver) {
        return sessionRepository.findFirstByDriverIdAndStatus(driver.getId(), "ACTIVE")
                .map(this::mapSessionToResponse)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<SessionResponse> getSessionsForUser(User user) {
        if (user.getRole() == Role.DRIVER) {
            return sessionRepository.findByDriverIdOrderByCreatedAtDesc(user.getId()).stream()
                    .map(this::mapSessionToResponse)
                    .collect(Collectors.toList());
        } else if (user.getRole() == Role.COMPANY_ADMIN) {
            if (user.getCompany() == null) return List.of();
            return sessionRepository.findByCompanyId(user.getCompany().getId()).stream()
                    .map(this::mapSessionToResponse)
                    .collect(Collectors.toList());
        } else {
            return sessionRepository.findAll().stream()
                    .map(this::mapSessionToResponse)
                    .collect(Collectors.toList());
        }
    }

    public void validateSessionAccess(ChargingSession session, User user) {
        if (user.getRole() == Role.PLATFORM_ADMIN) return;

        if (user.getRole() == Role.DRIVER) {
            if (!session.getDriver().getId().equals(user.getId())) {
                throw new UnauthorizedException("Unauthorized: You do not own this session.");
            }
        } else if (user.getRole() == Role.COMPANY_ADMIN) {
            if (user.getCompany() == null || !user.getCompany().getId().equals(session.getCharger().getStation().getCompany().getId())) {
                throw new UnauthorizedException("Unauthorized: Session belongs to another company.");
            }
        }
    }

    public SessionResponse mapSessionToResponse(ChargingSession session) {
        SessionResponse res = new SessionResponse();
        res.setId(session.getId());
        res.setSessionCode(session.getSessionCode());
        res.setBookingId(session.getBooking().getId());
        res.setBookingCode(session.getBooking().getBookingCode());
        res.setChargerId(session.getCharger().getId());
        res.setChargerIdentifier(session.getCharger().getChargerIdentifier());
        res.setChargerType(session.getCharger().getChargerType());
        res.setPowerRatingKw(session.getCharger().getPowerRatingKw());
        res.setStationName(session.getCharger().getStation().getName());
        res.setStationAddress(session.getCharger().getStation().getAddress());
        res.setDriverId(session.getDriver().getId());
        res.setDriverName(session.getDriver().getFullName());
        res.setStartTime(session.getStartTime());
        res.setEndTime(session.getEndTime());

        LocalDateTime end = session.getEndTime() != null ? session.getEndTime() : LocalDateTime.now();
        res.setElapsedMinutes(Duration.between(session.getStartTime(), end).toMinutes());

        res.setEnergyConsumedKwh(session.getEnergyConsumedKwh());
        res.setInitialBatteryPct(session.getInitialBatteryPct());
        res.setCurrentBatteryPct(session.getCurrentBatteryPct());
        res.setStatus(session.getStatus());

        return res;
    }
}
