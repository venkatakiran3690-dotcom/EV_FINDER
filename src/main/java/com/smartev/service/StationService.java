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

@Service
public class StationService {

    private final StationRepository stationRepository;
    private final ChargerRepository chargerRepository;
    private final CompanyRepository companyRepository;
    private final TariffRepository tariffRepository;
    private final ReviewRepository reviewRepository;
    private final GeofenceService geofenceService;

    public StationService(StationRepository stationRepository,
                          ChargerRepository chargerRepository,
                          CompanyRepository companyRepository,
                          TariffRepository tariffRepository,
                          ReviewRepository reviewRepository,
                          GeofenceService geofenceService) {
        this.stationRepository = stationRepository;
        this.chargerRepository = chargerRepository;
        this.companyRepository = companyRepository;
        this.tariffRepository = tariffRepository;
        this.reviewRepository = reviewRepository;
        this.geofenceService = geofenceService;
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
        }
    }
}
