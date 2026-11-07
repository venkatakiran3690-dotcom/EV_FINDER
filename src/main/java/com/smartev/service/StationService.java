package com.smartev.service;

import com.smartev.dto.ChargerCreateRequest;
import com.smartev.dto.ChargerResponse;
import com.smartev.dto.StationCreateRequest;
import com.smartev.dto.StationResponse;
import com.smartev.entity.*;
import com.smartev.exception.BadRequestException;
import com.smartev.exception.ResourceNotFoundException;
import com.smartev.exception.UnauthorizedException;
import com.smartev.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class StationService {

    private final StationRepository stationRepository;
    private final ChargerRepository chargerRepository;
    private final CompanyRepository companyRepository;
    private final TariffRepository tariffRepository;
    private final ReviewRepository reviewRepository;
    private final GeofenceService geofenceService;
    private final BookingRepository bookingRepository;
    private final BillRepository billRepository;
    private final ChargingSessionRepository sessionRepository;
    private final PaymentRepository paymentRepository;
    private final FavoriteRepository favoriteRepository;
    private final MaintenanceRecordRepository maintenanceRecordRepository;
    private final StationManagerAssignmentRepository assignmentRepository;

    public StationService(StationRepository stationRepository,
                          ChargerRepository chargerRepository,
                          CompanyRepository companyRepository,
                          TariffRepository tariffRepository,
                          ReviewRepository reviewRepository,
                          GeofenceService geofenceService) {
        this(stationRepository, chargerRepository, companyRepository, tariffRepository,
             reviewRepository, geofenceService, null, null, null, null, null, null, null);
    }

    @Autowired
    public StationService(StationRepository stationRepository,
                          ChargerRepository chargerRepository,
                          CompanyRepository companyRepository,
                          TariffRepository tariffRepository,
                          ReviewRepository reviewRepository,
                          GeofenceService geofenceService,
                          @Autowired(required = false) BookingRepository bookingRepository,
                          @Autowired(required = false) BillRepository billRepository,
                          @Autowired(required = false) ChargingSessionRepository sessionRepository,
                          @Autowired(required = false) PaymentRepository paymentRepository,
                          @Autowired(required = false) FavoriteRepository favoriteRepository,
                          @Autowired(required = false) MaintenanceRecordRepository maintenanceRecordRepository,
                          @Autowired(required = false) StationManagerAssignmentRepository assignmentRepository) {
        this.stationRepository = stationRepository;
        this.chargerRepository = chargerRepository;
        this.companyRepository = companyRepository;
        this.tariffRepository = tariffRepository;
        this.reviewRepository = reviewRepository;
        this.geofenceService = geofenceService;
        this.bookingRepository = bookingRepository;
        this.billRepository = billRepository;
        this.sessionRepository = sessionRepository;
        this.paymentRepository = paymentRepository;
        this.favoriteRepository = favoriteRepository;
        this.maintenanceRecordRepository = maintenanceRecordRepository;
        this.assignmentRepository = assignmentRepository;
    }

    @Transactional(readOnly = true)
    public List<StationResponse> getAllStations(String query, String chargerType, String connectorType,
                                               Double minPower, Double driverLat, Double driverLon) {
        List<Station> stations;
        if (query != null && !query.trim().isEmpty()) {
            stations = stationRepository.searchStations(query.trim());
        } else {
            stations = stationRepository.findAll();
        }

        List<StationResponse> responses = stations.stream()
                .map(station -> buildStationResponse(station, driverLat, driverLon))
                .filter(res -> {
                    if (chargerType != null && !chargerType.equalsIgnoreCase("ALL")) {
                        boolean match = res.getChargers().stream()
                                .anyMatch(c -> c.getChargerType().equalsIgnoreCase(chargerType));
                        if (!match) return false;
                    }
                    if (connectorType != null && !connectorType.equalsIgnoreCase("ALL")) {
                        boolean match = res.getChargers().stream()
                                .anyMatch(c -> c.getConnectorType().equalsIgnoreCase(connectorType));
                        if (!match) return false;
                    }
                    if (minPower != null && minPower > 0) {
                        boolean match = res.getChargers().stream()
                                .anyMatch(c -> c.getPowerRatingKw() >= minPower);
                        if (!match) return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());

        // Sort by distance if driver location is provided
        if (driverLat != null && driverLon != null) {
            responses.sort(Comparator.comparing(r -> r.getDistanceKm() != null ? r.getDistanceKm() : Double.MAX_VALUE));
        }

        return responses;
    }

    @Transactional(readOnly = true)
    public StationResponse getStationById(Long stationId, Double driverLat, Double driverLon) {
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new ResourceNotFoundException("Charging station not found with ID: " + stationId));
        return buildStationResponse(station, driverLat, driverLon);
    }

    @Transactional(readOnly = true)
    public List<ChargerResponse> getChargersByStation(Long stationId) {
        return chargerRepository.findByStationId(stationId).stream()
                .map(this::mapChargerToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public StationResponse createStation(StationCreateRequest request, User currentUser) {
        Company company;
        if (currentUser.getRole() == Role.PLATFORM_ADMIN) {
            if (request.getCompanyId() == null) {
                throw new BadRequestException("Company ID is required when Platform Admin creates a station.");
            }
            company = companyRepository.findById(request.getCompanyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Company not found with ID: " + request.getCompanyId()));
        } else if (currentUser.getRole() == Role.COMPANY_ADMIN) {
            company = currentUser.getCompany();
            if (company == null) {
                throw new UnauthorizedException("Company Admin does not have an assigned company.");
            }
        } else {
            throw new UnauthorizedException("You are not authorized to create stations.");
        }

        Station station = new Station(
                company,
                request.getName(),
                request.getAddress(),
                request.getCity(),
                request.getState(),
                request.getPostalCode(),
                request.getLatitude(),
                request.getLongitude(),
                0
        );

        Station saved = stationRepository.save(station);
        return buildStationResponse(saved, null, null);
    }

    @Transactional
    public ChargerResponse addChargerToStation(Long stationId, ChargerCreateRequest request, User currentUser) {
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new ResourceNotFoundException("Station not found with ID: " + stationId));

        validateCompanyAccess(station, currentUser);

        Charger charger = new Charger(
                station,
                request.getChargerIdentifier(),
                request.getChargerType().toUpperCase(),
                request.getConnectorType(),
                request.getPowerRatingKw(),
                request.getOperationalStatus() != null ? request.getOperationalStatus() : "OPERATIONAL",
                "AVAILABLE"
        );

        Charger saved = chargerRepository.save(charger);
        station.setTotalChargers(station.getTotalChargers() + 1);
        stationRepository.save(station);

        return mapChargerToResponse(saved);
    }

    @Transactional
    public ChargerResponse updateChargerStatus(Long chargerId, String operationalStatus, String availabilityStatus, User currentUser) {
        Charger charger = chargerRepository.findById(chargerId)
                .orElseThrow(() -> new ResourceNotFoundException("Charger not found with ID: " + chargerId));

        validateCompanyAccess(charger.getStation(), currentUser);

        if (operationalStatus != null) {
            charger.setOperationalStatus(operationalStatus.toUpperCase());
            if ("OUT_OF_SERVICE".equalsIgnoreCase(operationalStatus)) {
                charger.setAvailabilityStatus("UNAVAILABLE");
            }
        }
        if (availabilityStatus != null && !"OUT_OF_SERVICE".equalsIgnoreCase(charger.getOperationalStatus())) {
            charger.setAvailabilityStatus(availabilityStatus.toUpperCase());
        }

        return mapChargerToResponse(chargerRepository.save(charger));
    }

    public StationResponse buildStationResponse(Station station, Double driverLat, Double driverLon) {
        StationResponse response = new StationResponse();
        response.setId(station.getId());
        response.setCompanyId(station.getCompany().getId());
        response.setCompanyName(station.getCompany().getName());
        response.setName(station.getName());
        response.setAddress(station.getAddress());
        response.setCity(station.getCity());
        response.setState(station.getState());
        response.setPostalCode(station.getPostalCode());
        response.setLatitude(station.getLatitude());
        response.setLongitude(station.getLongitude());
        response.setTotalChargers(station.getTotalChargers());
        response.setOperationalStatus(station.getOperationalStatus());

        // Distance calculation
        if (driverLat != null && driverLon != null) {
            try {
                double distance = geofenceService.calculateDistance(driverLat, driverLon, station.getLatitude(), station.getLongitude());
                response.setDistanceKm(distance);
            } catch (Exception ignored) {
                response.setDistanceKm(null);
            }
        }

        // Fetch chargers
        List<Charger> chargers = chargerRepository.findByStationId(station.getId());
        response.setChargers(chargers.stream().map(this::mapChargerToResponse).collect(Collectors.toList()));

        // Calculate occupancy metrics
        // Total Operational Chargers (denominator)
        long operationalCount = chargers.stream()
                .filter(c -> "OPERATIONAL".equalsIgnoreCase(c.getOperationalStatus()))
                .count();

        // Occupied Chargers
        long occupiedCount = chargers.stream()
                .filter(c -> "OPERATIONAL".equalsIgnoreCase(c.getOperationalStatus())
                          && "OCCUPIED".equalsIgnoreCase(c.getAvailabilityStatus()))
                .count();

        // Available Chargers
        long availableCount = chargers.stream()
                .filter(c -> "OPERATIONAL".equalsIgnoreCase(c.getOperationalStatus())
                          && "AVAILABLE".equalsIgnoreCase(c.getAvailabilityStatus()))
                .count();

        response.setOperationalChargersCount(operationalCount);
        response.setAvailableChargersCount(availableCount);

        // Occupancy Percentage = Occupied / Total Operational * 100
        if (operationalCount > 0) {
            double occupancy = ((double) occupiedCount / operationalCount) * 100.0;
            response.setOccupancyPercentage(BigDecimal.valueOf(occupancy).setScale(1, RoundingMode.HALF_UP).doubleValue());
        } else {
            response.setOccupancyPercentage(0.0);
        }

        // Estimated waiting time (heuristic calculation for academic demo)
        // If chargers are available, wait is 0 mins. Otherwise, occupied * 20 mins / operationalCount
        if (availableCount > 0) {
            response.setEstimatedWaitMinutes(0);
        } else if (operationalCount > 0) {
            int wait = (int) Math.round(((double) occupiedCount * 25.0) / operationalCount);
            response.setEstimatedWaitMinutes(wait);
        } else {
            response.setEstimatedWaitMinutes(0);
        }

        // Average rating
        Double avgRating = reviewRepository.getAverageRatingForStation(station.getId());
        response.setAverageRating(BigDecimal.valueOf(avgRating != null ? avgRating : 0.0)
                .setScale(1, RoundingMode.HALF_UP).doubleValue());

        // Base tariff preview
        List<Tariff> tariffs = tariffRepository.findApplicableTariffs(station.getCompany().getId(), station.getId(), "ALL");
        if (!tariffs.isEmpty()) {
            response.setBaseTariffPerKwh(tariffs.get(0).getPricePerKwh());
        } else {
            response.setBaseTariffPerKwh(BigDecimal.valueOf(18.50)); // Default demo fallback
        }

        return response;
    }

    private ChargerResponse mapChargerToResponse(Charger charger) {
        return new ChargerResponse(
                charger.getId(),
                charger.getStation().getId(),
                charger.getChargerIdentifier(),
                charger.getChargerType(),
                charger.getConnectorType(),
                charger.getPowerRatingKw(),
                charger.getOperationalStatus(),
                charger.getAvailabilityStatus()
        );
    }

    public void validateCompanyAccess(Station station, User user) {
        if (user.getRole() == Role.PLATFORM_ADMIN) {
            return;
        }
        if (user.getRole() == Role.COMPANY_ADMIN) {
            if (user.getCompany() == null || !user.getCompany().getId().equals(station.getCompany().getId())) {
                throw new UnauthorizedException("Unauthorized: Station belongs to a different charging company.");
            }
        } else if (user.getRole() == Role.STATION_MANAGER) {
            if (user.getCompany() == null || !user.getCompany().getId().equals(station.getCompany().getId())) {
                throw new UnauthorizedException("Unauthorized: Station belongs to a different charging company.");
            }
            boolean isAssigned = assignmentRepository != null && assignmentRepository.existsByUserIdAndStationId(user.getId(), station.getId());
            if (!isAssigned) {
                throw new UnauthorizedException("Unauthorized: Station is not assigned to this station manager.");
            }
        } else {
            throw new UnauthorizedException("Unauthorized: You do not have permission to manage this station.");
        }
    }

    @Transactional(readOnly = true)
    public List<StationResponse> getAssignedStationsForManager(User manager) {
        if (manager.getRole() == Role.PLATFORM_ADMIN) {
            return stationRepository.findAll().stream()
                    .map(s -> buildStationResponse(s, null, null))
                    .collect(Collectors.toList());
        }
        if (manager.getRole() == Role.COMPANY_ADMIN) {
            if (manager.getCompany() == null) return List.of();
            return stationRepository.findByCompanyId(manager.getCompany().getId()).stream()
                    .map(s -> buildStationResponse(s, null, null))
                    .collect(Collectors.toList());
        }
        if (manager.getRole() == Role.STATION_MANAGER) {
            if (assignmentRepository == null) return List.of();
            List<StationManagerAssignment> assignments = assignmentRepository.findByUserId(manager.getId());
            return assignments.stream()
                    .map(a -> buildStationResponse(a.getStation(), null, null))
                    .collect(Collectors.toList());
        }
        throw new UnauthorizedException("Unauthorized: Not a station manager or administrator.");
    }

    @Transactional
    public StationResponse updateStation(Long stationId, StationCreateRequest request, User currentUser) {
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new ResourceNotFoundException("Station not found with ID: " + stationId));

        validateCompanyAccess(station, currentUser);

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            station.setName(request.getName().trim());
        }
        if (request.getAddress() != null && !request.getAddress().trim().isEmpty()) {
            station.setAddress(request.getAddress().trim());
        }
        if (request.getCity() != null && !request.getCity().trim().isEmpty()) {
            station.setCity(request.getCity().trim());
        }
        if (request.getState() != null) {
            station.setState(request.getState().trim());
        }
        if (request.getPostalCode() != null) {
            station.setPostalCode(request.getPostalCode().trim());
        }
        if (request.getLatitude() != null) {
            station.setLatitude(request.getLatitude());
        }
        if (request.getLongitude() != null) {
            station.setLongitude(request.getLongitude());
        }
        if (request.getOperationalStatus() != null && !request.getOperationalStatus().trim().isEmpty()) {
            station.setOperationalStatus(request.getOperationalStatus().trim().toUpperCase());
        }

        Station updated = stationRepository.save(station);
        return buildStationResponse(updated, null, null);
    }

    @Transactional
    public void deleteStation(Long stationId, User currentUser) {
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new ResourceNotFoundException("Station not found with ID: " + stationId));

        validateCompanyAccess(station, currentUser);

        // Check if there are active bookings
        if (bookingRepository != null) {
            List<Booking> bookings = bookingRepository.findByStationIdOrderByStartTimeDesc(stationId);
            boolean hasActive = bookings.stream().anyMatch(b ->
                    "PENDING_PAYMENT".equalsIgnoreCase(b.getStatus()) ||
                    "CONFIRMED".equalsIgnoreCase(b.getStatus()) ||
                    "IN_PROGRESS".equalsIgnoreCase(b.getStatus())
            );
            if (hasActive) {
                throw new BadRequestException("Cannot delete station with active confirmed or pending bookings. Please cancel them first.");
            }

            // Clean up past bookings and their sessions/bills/payments
            for (Booking b : bookings) {
                if (sessionRepository != null) {
                    sessionRepository.findByBookingId(b.getId()).ifPresent(session -> {
                        if (billRepository != null) {
                            billRepository.findBySessionId(session.getId()).ifPresent(billRepository::delete);
                        }
                        sessionRepository.delete(session);
                    });
                }
                if (paymentRepository != null) {
                    paymentRepository.findByBookingId(b.getId()).forEach(paymentRepository::delete);
                }
                bookingRepository.delete(b);
            }
        }

        // Clean up any stray bills for this station
        if (billRepository != null) {
            billRepository.findByStationIdOrderByCreatedAtDesc(stationId).forEach(billRepository::delete);
        }

        // Clean up any stray sessions for this station
        if (sessionRepository != null) {
            sessionRepository.findByStationId(stationId).forEach(sessionRepository::delete);
        }

        // Clean up reviews
        if (reviewRepository != null) {
            reviewRepository.findByStationIdOrderByCreatedAtDesc(stationId).forEach(reviewRepository::delete);
        }

        // Clean up favorites
        if (favoriteRepository != null) {
            favoriteRepository.findByStationId(stationId).forEach(favoriteRepository::delete);
        }

        // Clean up tariffs
        if (tariffRepository != null) {
            tariffRepository.findByStationId(stationId).forEach(tariffRepository::delete);
        }

        // Clean up maintenance records
        if (maintenanceRecordRepository != null) {
            maintenanceRecordRepository.findByStationIdOrderByCreatedAtDesc(stationId).forEach(maintenanceRecordRepository::delete);
        }

        // Clean up station manager assignments
        if (assignmentRepository != null) {
            assignmentRepository.findByStationId(stationId).forEach(assignmentRepository::delete);
        }

        // Clean up chargers
        if (chargerRepository != null) {
            chargerRepository.findByStationId(stationId).forEach(chargerRepository::delete);
        }

        // Delete station
        stationRepository.delete(station);
    }

    @Transactional
    public void deleteCharger(Long chargerId, User currentUser) {
        Charger charger = chargerRepository.findById(chargerId)
                .orElseThrow(() -> new ResourceNotFoundException("Charger not found with ID: " + chargerId));

        validateCompanyAccess(charger.getStation(), currentUser);

        // Check active bookings on this charger
        if (bookingRepository != null) {
            List<Booking> conflicts = bookingRepository.findConflictingBookings(
                    chargerId, LocalDateTime.now(), LocalDateTime.now().plusYears(1));
            if (!conflicts.isEmpty()) {
                throw new BadRequestException("Cannot delete charger with upcoming active bookings.");
            }
        }

        Station station = charger.getStation();
        chargerRepository.delete(charger);
        if (station.getTotalChargers() > 0) {
            station.setTotalChargers(station.getTotalChargers() - 1);
            stationRepository.save(station);
        }
    }
}
