package com.smartev.service;

import com.smartev.dto.BookingRequest;
import com.smartev.dto.BookingResponse;
import com.smartev.entity.*;
import com.smartev.exception.BadRequestException;
import com.smartev.exception.ResourceNotFoundException;
import com.smartev.exception.SlotConflictException;
import com.smartev.exception.UnauthorizedException;
import com.smartev.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final StationRepository stationRepository;
    private final ChargerRepository chargerRepository;
    private final VehicleRepository vehicleRepository;
    private final NotificationRepository notificationRepository;
    private final GeofenceService geofenceService;
    private final BigDecimal demoTokenAmount;

    public BookingService(BookingRepository bookingRepository,
                          StationRepository stationRepository,
                          ChargerRepository chargerRepository,
                          VehicleRepository vehicleRepository,
                          NotificationRepository notificationRepository,
                          GeofenceService geofenceService,
                          @Value("${app.payment.demo-token-amount:150.00}") BigDecimal demoTokenAmount) {
        this.bookingRepository = bookingRepository;
        this.stationRepository = stationRepository;
        this.chargerRepository = chargerRepository;
        this.vehicleRepository = vehicleRepository;
        this.notificationRepository = notificationRepository;
        this.geofenceService = geofenceService;
        this.demoTokenAmount = demoTokenAmount != null ? demoTokenAmount.setScale(2, java.math.RoundingMode.HALF_UP) : new BigDecimal("150.00");
    }

    /**
     * Creates a new slot pre-booking with strict server-side geofencing and concurrency-safe conflict detection.
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public synchronized BookingResponse createBooking(BookingRequest request, User driver) {
        LocalDateTime requestedStart = request.getStartTime();
        if (requestedStart == null) {
            throw new BadRequestException("Booking start time is required.");
        }

        // Allow up to 5 minutes clock skew for current time, but reject bookings placed in the past
        if (requestedStart.isBefore(LocalDateTime.now().minusMinutes(5))) {
            throw new BadRequestException("Booking start time cannot be in the past.");
        }

        LocalDateTime requestedEnd = requestedStart.plusMinutes(request.getDurationMinutes());

        // 1. Fetch and validate Station
        Station station = stationRepository.findById(request.getStationId())
                .orElseThrow(() -> new ResourceNotFoundException("Charging station not found with ID: " + request.getStationId()));

        if (!"OPERATIONAL".equalsIgnoreCase(station.getOperationalStatus())) {
            throw new BadRequestException("Station '" + station.getName() + "' is currently " + station.getOperationalStatus() + " and not accepting bookings.");
        }

        // 2. Fetch and validate Charger
        Charger charger = chargerRepository.findById(request.getChargerId())
                .orElseThrow(() -> new ResourceNotFoundException("Charger not found with ID: " + request.getChargerId()));

        if (!charger.getStation().getId().equals(station.getId())) {
            throw new BadRequestException("Charger does not belong to the selected station.");
        }

        if ("OUT_OF_SERVICE".equalsIgnoreCase(charger.getOperationalStatus())) {
            throw new BadRequestException("Charger '" + charger.getChargerIdentifier() + "' is OUT OF SERVICE and cannot be booked.");
        }

        // 3. Server-side Haversine 30 km Geofence Validation
        double distanceKm = geofenceService.validateGeofence(
                request.getDriverLatitude(),
                request.getDriverLongitude(),
                station.getLatitude(),
                station.getLongitude()
        );

        // 4. Concurrency-Safe Conflict Detection
        // Half-open interval rule: existingStart < requestedEnd AND existingEnd > requestedStart
        List<Booking> conflicts = bookingRepository.findConflictingBookings(charger.getId(), requestedStart, requestedEnd);
        if (!conflicts.isEmpty()) {
            Booking conflict = conflicts.get(0);
            throw new SlotConflictException(String.format(
                    "Time slot conflict: Charger '%s' is already reserved from %s to %s.",
                    charger.getChargerIdentifier(), conflict.getStartTime(), conflict.getEndTime()
            ));
        }

        // 5. Optional Vehicle association
        Vehicle vehicle = null;
        if (request.getVehicleId() != null) {
            vehicle = vehicleRepository.findByIdAndDriverId(request.getVehicleId(), driver.getId())
                    .orElse(null);
        }

        // 6. Generate Unique Booking Code
        String bookingCode = "BK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Booking booking = new Booking(
                bookingCode,
                driver,
                vehicle,
                station,
                charger,
                requestedStart,
                requestedEnd,
                distanceKm,
                demoTokenAmount
        );

        Booking saved = bookingRepository.save(booking);

        // Create In-App Notification
        Notification notification = new Notification(
                driver,
                "Booking Created: " + bookingCode,
                String.format("Slot pre-booked at %s (%s). Please complete demo token payment of ₹%.2f to confirm.",
                        station.getName(), charger.getChargerIdentifier(), demoTokenAmount),
                "BOOKING_REMINDER"
        );
        notificationRepository.save(notification);

        return mapBookingToResponse(saved);
    }

    @Transactional(readOnly = true)
    public BookingResponse getBookingById(Long bookingId, User currentUser) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + bookingId));

        validateBookingAccess(booking, currentUser);
        return mapBookingToResponse(booking);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsForUser(User user) {
        if (user.getRole() == Role.DRIVER) {
            return bookingRepository.findByDriverIdOrderByCreatedAtDesc(user.getId()).stream()
                    .map(this::mapBookingToResponse)
                    .collect(Collectors.toList());
        } else if (user.getRole() == Role.COMPANY_ADMIN) {
            if (user.getCompany() == null) {
                return List.of();
            }
            return bookingRepository.findByStationCompanyIdOrderByStartTimeDesc(user.getCompany().getId()).stream()
                    .map(this::mapBookingToResponse)
                    .collect(Collectors.toList());
        } else {
            return bookingRepository.findAll().stream()
                    .map(this::mapBookingToResponse)
                    .collect(Collectors.toList());
        }
    }

    @Transactional
    public BookingResponse cancelBooking(Long bookingId, User driver) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + bookingId));

        if (!booking.getDriver().getId().equals(driver.getId()) && driver.getRole() != Role.PLATFORM_ADMIN) {
            throw new UnauthorizedException("You are not authorized to cancel this booking.");
        }

        if ("COMPLETED".equalsIgnoreCase(booking.getStatus()) || "IN_PROGRESS".equalsIgnoreCase(booking.getStatus())) {
            throw new BadRequestException("Cannot cancel a booking that is already in progress or completed.");
        }

        if ("CANCELLED".equalsIgnoreCase(booking.getStatus())) {
            throw new BadRequestException("Booking is already cancelled.");
        }

        // Cancellation Rule: If confirmed, must be at least 30 minutes before start time
        if ("CONFIRMED".equalsIgnoreCase(booking.getStatus())) {
            if (LocalDateTime.now().plusMinutes(30).isAfter(booking.getStartTime())) {
                throw new BadRequestException("Cancellation is only permitted at least 30 minutes before the scheduled start time.");
            }
        }

        booking.setStatus("CANCELLED");
        Booking updated = bookingRepository.save(booking);

        // Update charger availability if it was reserved
        Charger charger = booking.getCharger();
        if ("RESERVED".equalsIgnoreCase(charger.getAvailabilityStatus())) {
            charger.setAvailabilityStatus("AVAILABLE");
            chargerRepository.save(charger);
        }

        // Notification
        Notification notification = new Notification(
                driver,
                "Booking Cancelled: " + booking.getBookingCode(),
                "Your booking for " + booking.getStation().getName() + " has been successfully cancelled.",
                "STATUS_UPDATE"
        );
        notificationRepository.save(notification);

        return mapBookingToResponse(updated);
    }

    public void validateBookingAccess(Booking booking, User user) {
        if (user.getRole() == Role.PLATFORM_ADMIN) return;

        if (user.getRole() == Role.DRIVER) {
            if (!booking.getDriver().getId().equals(user.getId())) {
                throw new UnauthorizedException("Unauthorized: You do not own this booking.");
            }
        } else if (user.getRole() == Role.COMPANY_ADMIN) {
            if (user.getCompany() == null || !user.getCompany().getId().equals(booking.getStation().getCompany().getId())) {
                throw new UnauthorizedException("Unauthorized: Booking belongs to another company's station.");
            }
        }
    }

    public BookingResponse mapBookingToResponse(Booking booking) {
        BookingResponse res = new BookingResponse();
        res.setId(booking.getId());
        res.setBookingCode(booking.getBookingCode());
        res.setDriverId(booking.getDriver().getId());
        res.setDriverName(booking.getDriver().getFullName());

        if (booking.getVehicle() != null) {
            res.setVehicleId(booking.getVehicle().getId());
            res.setVehicleInfo(booking.getVehicle().getMake() + " " + booking.getVehicle().getModel() + " (" + booking.getVehicle().getLicensePlate() + ")");
        }

        res.setStationId(booking.getStation().getId());
        res.setStationName(booking.getStation().getName());
        res.setStationAddress(booking.getStation().getAddress() + ", " + booking.getStation().getCity());

        res.setChargerId(booking.getCharger().getId());
        res.setChargerIdentifier(booking.getCharger().getChargerIdentifier());
        res.setChargerType(booking.getCharger().getChargerType());
        res.setConnectorType(booking.getCharger().getConnectorType());

        res.setStartTime(booking.getStartTime());
        res.setEndTime(booking.getEndTime());
        res.setCalculatedDistanceKm(booking.getCalculatedDistanceKm());
        res.setTokenAmount(booking.getTokenAmount());
        res.setStatus(booking.getStatus());
        res.setCreatedAt(booking.getCreatedAt());

        return res;
    }
}
