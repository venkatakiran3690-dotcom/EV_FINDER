package com.smartev.service;

import com.smartev.dto.ReviewResponse;
import com.smartev.dto.StationResponse;
import com.smartev.entity.*;
import com.smartev.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Station Reviews, Ratings & Dynamic Count Unit Tests")
class StationReviewsAndRatingsTest {

    private StationRepository stationRepository;
    private ChargerRepository chargerRepository;
    private TariffRepository tariffRepository;
    private GeofenceService geofenceService;
    private ReviewRepository reviewRepository;
    private StationService stationService;

    private Station sampleStation;
    private Company sampleCompany;
    private Charger sampleCharger;
    private User sampleDriver;

    @BeforeEach
    void setUp() {
        stationRepository = mock(StationRepository.class);
        chargerRepository = mock(ChargerRepository.class);
        tariffRepository = mock(TariffRepository.class);
        geofenceService = mock(GeofenceService.class);
        reviewRepository = mock(ReviewRepository.class);

        stationService = new StationService(
                stationRepository,
                chargerRepository,
                mock(CompanyRepository.class),
                tariffRepository,
                reviewRepository,
                geofenceService
        );

        sampleCompany = new Company("Tata Power EZ Charge", "CPO-IND-2022-001", "support@tatapower.com", "+91 1800 209 5161", "Mumbai");
        sampleCompany.setId(1L);

        sampleStation = new Station(
                sampleCompany,
                "Tata Power EV Hub - Cyber Towers",
                "Building 1, Hitec City Main Road",
                "Hyderabad",
                "Telangana",
                "500081",
                17.4504,
                78.3808,
                4
        );
        sampleStation.setId(6L);

        sampleCharger = new Charger(sampleStation, "TP-HYD-HITEC-DC-120KW-01", "DC", "CCS2", 120.0, "OPERATIONAL", "AVAILABLE");
        sampleCharger.setId(18L);

        sampleDriver = new User("driver@smartev.com", "hash", "Venkata Kiran", "+91 98765 43210", Role.DRIVER, null);
        sampleDriver.setId(5L);
    }

    @Test
    @DisplayName("buildStationResponse dynamically populates average rating and total review count from database")
    void testStationResponseIncludesRatingAndReviewCount() {
        when(chargerRepository.findByStationId(6L)).thenReturn(List.of(sampleCharger));
        when(reviewRepository.getAverageRatingForStation(6L)).thenReturn(4.8);
        when(reviewRepository.countByStationId(6L)).thenReturn(5L);
        when(tariffRepository.findApplicableTariffs(1L, 6L, "ALL")).thenReturn(List.of(
                new Tariff(sampleCompany, sampleStation, "ALL", BigDecimal.valueOf(19.50), BigDecimal.valueOf(25.0), BigDecimal.valueOf(18.0))
        ));

        StationResponse response = stationService.buildStationResponse(sampleStation, null, null);

        assertNotNull(response);
        assertEquals("Tata Power EV Hub - Cyber Towers", response.getName());
        assertEquals(4.8, response.getAverageRating());
        assertEquals(5L, response.getTotalReviews());
        assertEquals(BigDecimal.valueOf(19.50), response.getBaseTariffPerKwh());
        verify(reviewRepository, times(1)).getAverageRatingForStation(6L);
        verify(reviewRepository, times(1)).countByStationId(6L);
    }

    @Test
    @DisplayName("buildStationResponse returns 0.0 rating and 0 count when no reviews exist")
    void testStationResponseWithZeroReviews() {
        when(chargerRepository.findByStationId(6L)).thenReturn(List.of(sampleCharger));
        when(reviewRepository.getAverageRatingForStation(6L)).thenReturn(null);
        when(reviewRepository.countByStationId(6L)).thenReturn(0L);

        StationResponse response = stationService.buildStationResponse(sampleStation, null, null);

        assertNotNull(response);
        assertEquals(0.0, response.getAverageRating());
        assertEquals(0L, response.getTotalReviews());
    }

    @Test
    @DisplayName("Review entity correctly stores comment with [Simulated Demo Review] tag and timestamps")
    void testReviewEntityWithSimulatedTag() {
        String comment = "Excellent charging at Cyber Towers! [Simulated Demo Review]";
        Review review = new Review(sampleStation, sampleDriver, 5, comment);
        LocalDateTime past = LocalDateTime.now().minusDays(2);
        review.setCreatedAt(past);

        assertEquals(sampleStation, review.getStation());
        assertEquals(sampleDriver, review.getDriver());
        assertEquals(5, review.getRating());
        assertTrue(review.getComment().contains("[Simulated Demo Review]"));
        assertEquals(past, review.getCreatedAt());
    }
}
