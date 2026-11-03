package com.smartev.service;

import com.smartev.exception.GeofenceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class GeofenceService {

    private static final double EARTH_RADIUS_KM = 6371.0;

    private final double maxRadiusKm;

    public GeofenceService(@Value("${app.geofence.max-radius-km:30.0}") double maxRadiusKm) {
        this.maxRadiusKm = maxRadiusKm;
    }

    /**
     * Calculates great-circle distance between two geographic coordinates using the Haversine formula.
     *
     * @param lat1 Driver latitude in degrees (-90 to 90)
     * @param lon1 Driver longitude in degrees (-180 to 180)
     * @param lat2 Station latitude in degrees (-90 to 90)
     * @param lon2 Station longitude in degrees (-180 to 180)
     * @return Distance in kilometres rounded to 2 decimal places.
     */
    public double calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        validateCoordinates(lat1, lon1, "Driver");
        validateCoordinates(lat2, lon2, "Station");

        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);

        double deltaLat = lat2Rad - lat1Rad;
        double deltaLon = lon2Rad - lon1Rad;

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1Rad) * Math.cos(lat2Rad)
                * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double rawDistance = EARTH_RADIUS_KM * c;

        return BigDecimal.valueOf(rawDistance)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    /**
     * Enforces the 30 km software geofence rule for pre-booking charging slots.
     *
     * @param driverLat Driver current latitude
     * @param driverLon Driver current longitude
     * @param stationLat Station latitude
     * @param stationLon Station longitude
     * @return Calculated distance in km if within geofence.
     * @throws GeofenceException if distance exceeds maxRadiusKm.
     */
    public double validateGeofence(Double driverLat, Double driverLon, Double stationLat, Double stationLon) {
        double distanceKm = calculateDistance(driverLat, driverLon, stationLat, stationLon);

        if (distanceKm > maxRadiusKm) {
            throw new GeofenceException(String.format(
                    "Geofence restriction: Station is %.2f km away, which exceeds the permitted maximum pre-booking radius of %.1f km. Please select a closer station.",
                    distanceKm, maxRadiusKm
            ));
        }

        return distanceKm;
    }

    public void validateCoordinates(Double lat, Double lon, String label) {
        if (lat == null || lon == null) {
            throw new GeofenceException(label + " geographic coordinates cannot be null.");
        }
        if (lat < -90.0 || lat > 90.0) {
            throw new GeofenceException(label + " latitude " + lat + " is out of valid range [-90, +90].");
        }
        if (lon < -180.0 || lon > 180.0) {
            throw new GeofenceException(label + " longitude " + lon + " is out of valid range [-180, +180].");
        }
    }

    public double getMaxRadiusKm() {
        return maxRadiusKm;
    }
}
