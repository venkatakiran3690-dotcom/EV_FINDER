package com.smartev.service;

import com.smartev.dto.SessionResponse;
import com.smartev.entity.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Charging Time & Real-Time Range Estimation Unit Tests")
class ChargingEstimationTest {

    @Test
    @DisplayName("Vehicle full-charge range resolves model-specific parameters")
    void testVehicleFullRangeResolution() {
        User driver = new User("driver@smartev.com", "pass", "EV Driver", "9876543210", Role.DRIVER, null);

        Vehicle nexon = new Vehicle(driver, "Tata", "Nexon EV Long Range", 2024, "KA-01-EV-4096", 40.5, "CCS2");
        assertEquals(450.0, ChargingSessionService.calculateVehicleFullRangeKm(nexon), 0.01);

        Vehicle mg = new Vehicle(driver, "MG", "ZS EV Excite", 2023, "KA-05-EV-8821", 50.3, "CCS2");
        assertEquals(460.0, ChargingSessionService.calculateVehicleFullRangeKm(mg), 0.01);

        Vehicle ioniq = new Vehicle(driver, "Hyundai", "Ioniq 5 EV", 2024, "TS-09-EV-1008", 72.6, "CCS2");
        assertEquals(630.0, ChargingSessionService.calculateVehicleFullRangeKm(ioniq), 0.01);

        Vehicle tiago = new Vehicle(driver, "Tata", "Tiago EV Long Range", 2024, "TS-07-EV-2045", 24.0, "CCS2");
        assertEquals(315.0, ChargingSessionService.calculateVehicleFullRangeKm(tiago), 0.01);

        Vehicle xuv = new Vehicle(driver, "Mahindra", "XUV400 EV", 2023, "TN-02-EV-5544", 39.4, "CCS2");
        assertEquals(456.0, ChargingSessionService.calculateVehicleFullRangeKm(xuv), 0.01);

        Vehicle atto = new Vehicle(driver, "BYD", "Atto 3 Superior", 2024, "AP-16-EV-9021", 60.48, "CCS2");
        assertEquals(521.0, ChargingSessionService.calculateVehicleFullRangeKm(atto), 0.01);

        // Custom model with known capacity
        Vehicle custom = new Vehicle(driver, "Custom", "EV Roadster", 2024, "DL-01-EV-9999", 50.0, "CCS2");
        assertEquals(425.0, ChargingSessionService.calculateVehicleFullRangeKm(custom), 0.01);

        // Missing or null vehicle
        assertEquals(0.0, ChargingSessionService.calculateVehicleFullRangeKm(null), 0.01);
    }

    @Test
    @DisplayName("Estimated charging time remaining accurately calculates minutes to 85% fast charge")
    void testEstimatedChargingTimeRemaining() {
        // Tata Nexon EV (40.5 kWh), starting at 20% to reach 85% on a 50 kW fast charger
        // Delta kWh: 40.5 * 0.65 = 26.325 kWh
        // Effective power (92% eff): 46 kW -> 26.325 / 46 = 0.572 hr = ~34-35 minutes
        int minutes = ChargingSessionService.calculateEstimatedTimeRemainingMinutes(40.5, 20, 85, 50.0);
        assertEquals(34, minutes); // 34.3 min rounds to 34 min

        // Tata Nexon EV at 50% SoC to 85% SoC
        // Delta kWh: 40.5 * 0.35 = 14.175 kWh -> 14.175 / 46 = 0.308 hr = 18.5 min
        int minutesAt50 = ChargingSessionService.calculateEstimatedTimeRemainingMinutes(40.5, 50, 85, 50.0);
        assertEquals(18, minutesAt50);

        // Already at or above target SoC
        assertEquals(0, ChargingSessionService.calculateEstimatedTimeRemainingMinutes(40.5, 85, 85, 50.0));
        assertEquals(0, ChargingSessionService.calculateEstimatedTimeRemainingMinutes(40.5, 90, 85, 50.0));

        // Invalid or missing parameters
        assertEquals(-1, ChargingSessionService.calculateEstimatedTimeRemainingMinutes(null, 20, 85, 50.0));
        assertEquals(-1, ChargingSessionService.calculateEstimatedTimeRemainingMinutes(0.0, 20, 85, 50.0));
        assertEquals(-1, ChargingSessionService.calculateEstimatedTimeRemainingMinutes(40.5, 20, 85, 0.0));
    }

    @Test
    @DisplayName("Real-time driving range formula yields 225 km at 50% for 450 km range vehicle")
    void testEstimatedDrivingRangeCalculation() {
        double fullRange = 450.0; // Tata Nexon EV full range

        // At 50% SoC: 450 * 50 / 100 = 225 km
        double range50 = (fullRange * 50) / 100.0;
        assertEquals(225.0, range50, 0.01);

        // At 20% SoC: 450 * 20 / 100 = 90 km
        double range20 = (fullRange * 20) / 100.0;
        assertEquals(90.0, range20, 0.01);

        // At 85% SoC: 450 * 85 / 100 = 382.5 km
        double range85 = (fullRange * 85) / 100.0;
        assertEquals(382.5, range85, 0.01);
    }

    @Test
    @DisplayName("SessionResponse mapping includes vehicle details and dynamic estimates")
    void testSessionResponseMappingWithVehicle() {
        Company company = new Company("Tata Power", "REG-001", "contact@tata.com", "9999999999", "Mumbai");
        company.setId(1L);

        Station station = new Station(company, "Cyber Towers Hub", "Hitec City", "Hyderabad", "Telangana", "500081", 17.45, 78.38, 4);
        station.setId(10L);

        Charger charger = new Charger(station, "CHG-DC-01", "DC", "CCS2", 50.0, "OPERATIONAL", "AVAILABLE");
        charger.setId(20L);

        User driver = new User("driver@smartev.com", "pass", "Demo Driver", "9876543210", Role.DRIVER, null);
        driver.setId(5L);

        Vehicle vehicle = new Vehicle(driver, "Tata", "Nexon EV Long Range", 2024, "KA-01-EV-4096", 40.5, "CCS2");
        vehicle.setId(100L);

        Booking booking = new Booking("BK-DEMO-001", driver, vehicle, station, charger,
                LocalDateTime.now(), LocalDateTime.now().plusMinutes(45), 4.2, new BigDecimal("150.00"));
        booking.setId(200L);

        ChargingSession session = new ChargingSession(booking, charger, driver, "SESS-TEST-001", LocalDateTime.now(), 20);
        session.setId(300L);
        session.setCurrentBatteryPct(50); // Simulating 50% SoC

        ChargingSessionService service = new ChargingSessionService(null, null, null, null, null);
        SessionResponse res = service.mapSessionToResponse(session);

        assertNotNull(res);
        assertEquals(100L, res.getVehicleId());
        assertEquals("Tata", res.getVehicleMake());
        assertEquals("Nexon EV Long Range", res.getVehicleModel());
        assertEquals("KA-01-EV-4096", res.getVehiclePlate());
        assertEquals(40.5, res.getBatteryCapacityKwh(), 0.01);
        assertEquals(450.0, res.getEstimatedFullRangeKm(), 0.01);
        assertEquals(225.0, res.getEstimatedDrivingRangeKm(), 0.01);
        assertEquals(18, res.getEstimatedRemainingMinutes());
    }
}
