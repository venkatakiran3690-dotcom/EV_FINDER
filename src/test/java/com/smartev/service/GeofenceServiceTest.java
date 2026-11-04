package com.smartev.service;

import com.smartev.exception.GeofenceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Haversine Distance & 30 km Geofence Verification Tests")
class GeofenceServiceTest {

    private GeofenceService geofenceService;

    @BeforeEach
    void setUp() {
        geofenceService = new GeofenceService(30.0);
    }

    @Test
    @DisplayName("Should accurately calculate distance between Bangalore Indiranagar and Koramangala (~5.5 km)")
    void testAccurateDistanceCalculation() {
        // Indiranagar: 12.9784, 77.6408
        // Koramangala: 12.9352, 77.6245
        double distance = geofenceService.calculateDistance(12.9784, 77.6408, 12.9352, 77.6245);
        assertTrue(distance > 4.5 && distance < 6.5, "Distance should be approximately 5.0 to 6.0 km, got: " + distance);
    }

    @Test
    @DisplayName("Should accept station within 30 km geofence")
    void testStationWithinGeofence() {
        // Distance ~5.5 km
        double distance = geofenceService.validateGeofence(12.9784, 77.6408, 12.9352, 77.6245);
        assertTrue(distance <= 30.0);
    }

    @Test
    @DisplayName("Should reject station beyond 30 km geofence with clear exception")
    void testStationBeyondGeofenceThrowsException() {
        // Bangalore Indiranagar (12.9784, 77.6408) to Mysore (12.2958, 76.6394) is ~135 km
        GeofenceException ex = assertThrows(GeofenceException.class, () ->
                geofenceService.validateGeofence(12.9784, 77.6408, 12.2958, 76.6394)
        );
        assertTrue(ex.getMessage().contains("exceeds the permitted maximum pre-booking radius"));
    }

    @Test
    @DisplayName("Should handle boundary condition around exactly 30 km")
    void testBoundaryConditionAround30Km() {
        // Coordinate pair roughly 30.0 km apart
        // 1 degree lat is ~111 km, so ~0.269 degrees lat is ~29.9 km
        double baseLat = 12.9700;
        double baseLon = 77.6000;
        double nearBoundaryLat = 12.9700 + (29.5 / 111.0); // ~29.5 km away

        assertDoesNotThrow(() -> geofenceService.validateGeofence(baseLat, baseLon, nearBoundaryLat, baseLon));

        double pastBoundaryLat = 12.9700 + (30.8 / 111.0); // ~30.8 km away
        assertThrows(GeofenceException.class, () ->
                geofenceService.validateGeofence(baseLat, baseLon, pastBoundaryLat, baseLon)
        );
    }

    @Test
    @DisplayName("Should reject invalid or null coordinates")
    void testInvalidCoordinates() {
        assertThrows(GeofenceException.class, () -> geofenceService.calculateDistance(null, 77.0, 12.0, 77.0));
        assertThrows(GeofenceException.class, () -> geofenceService.calculateDistance(95.0, 77.0, 12.0, 77.0)); // lat > 90
        assertThrows(GeofenceException.class, () -> geofenceService.calculateDistance(12.0, -185.0, 12.0, 77.0)); // lon < -180
    }
}
