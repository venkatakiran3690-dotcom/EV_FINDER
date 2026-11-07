package com.smartev.security;

import com.smartev.entity.*;
import com.smartev.exception.UnauthorizedException;
import com.smartev.repository.StationManagerAssignmentRepository;
import com.smartev.service.BookingService;
import com.smartev.service.ChargingSessionService;
import com.smartev.service.StationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Security, BCrypt Hashing & Multi-Tenant Data Isolation Tests")
class SecurityAndIsolationTest {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    @DisplayName("Should securely hash passwords with BCrypt and verify matching hashes")
    void testBCryptPasswordHashing() {
        String rawPassword = "StrongSecretPass@2026";
        String encodedHash = passwordEncoder.encode(rawPassword);

        assertNotNull(encodedHash);
        assertNotEquals(rawPassword, encodedHash);
        assertTrue(encodedHash.startsWith("$2a$") || encodedHash.startsWith("$2b$"));
        assertTrue(passwordEncoder.matches(rawPassword, encodedHash));
        assertFalse(passwordEncoder.matches("WrongPassword", encodedHash));
    }

    @Test
    @DisplayName("Should enforce strict multi-tenant company isolation: Company Admin A cannot access Company B station")
    void testCompanyDataIsolation() {
        Company companyA = new Company("Tata Power", "REG-A", "a@tata.com", "111", "Mumbai");
        companyA.setId(101L);

        Company companyB = new Company("Zeon Charging", "REG-B", "b@zeon.com", "222", "Coimbatore");
        companyB.setId(202L);

        User adminCompanyA = new User("adminA@smartev.com", "hash", "Admin A", "123", Role.COMPANY_ADMIN, companyA);
        adminCompanyA.setId(1L);

        Station stationCompanyB = new Station(companyB, "Zeon Hub", "Expressway", "Bangalore", "KA", "560100", 12.84, 77.66, 3);
        stationCompanyB.setId(500L);

        StationService stationService = new StationService(null, null, null, null, null, null);

        // Attempting to access station belonging to Company B as Company Admin A must throw UnauthorizedException
        UnauthorizedException ex = assertThrows(UnauthorizedException.class, () ->
                stationService.validateCompanyAccess(stationCompanyB, adminCompanyA)
        );
        assertTrue(ex.getMessage().contains("Station belongs to a different charging company"));
    }

    @Test
    @DisplayName("Platform Admin should have universal administrative oversight across companies")
    void testPlatformAdminOversight() {
        Company companyB = new Company("Zeon Charging", "REG-B", "b@zeon.com", "222", "Coimbatore");
        companyB.setId(202L);

        User platformAdmin = new User("admin@smartev.com", "hash", "Platform Admin", "000", Role.PLATFORM_ADMIN, null);
        platformAdmin.setId(99L);

        Station stationCompanyB = new Station(companyB, "Zeon Hub", "Expressway", "Bangalore", "KA", "560100", 12.84, 77.66, 3);

        StationService stationService = new StationService(null, null, null, null, null, null);

        assertDoesNotThrow(() ->
                stationService.validateCompanyAccess(stationCompanyB, platformAdmin)
        );
    }

    @Test
    @DisplayName("Station Manager can access explicitly assigned station")
    void testStationManagerCanAccessAssignedStation() {
        Company companyA = new Company("Tata Power", "REG-A", "a@tata.com", "111", "Mumbai");
        companyA.setId(101L);

        User manager = new User("mgr@smartev.com", "hash", "Manager Suresh", "123", Role.STATION_MANAGER, companyA);
        manager.setId(4L);

        Station station1 = new Station(companyA, "Tata Indiranagar Hub", "100ft Rd", "Bangalore", "KA", "560038", 12.97, 77.64, 4);
        station1.setId(10L);

        StationManagerAssignmentRepository mockAssignmentRepo = mock(StationManagerAssignmentRepository.class);
        when(mockAssignmentRepo.existsByUserIdAndStationId(4L, 10L)).thenReturn(true);

        StationService stationService = new StationService(null, null, null, null, null, null, null, null, null, null, null, null, mockAssignmentRepo);

        assertDoesNotThrow(() -> stationService.validateCompanyAccess(station1, manager));
    }

    @Test
    @DisplayName("Station Manager cannot access unassigned station of the same company")
    void testStationManagerCannotAccessUnassignedStationSameCompany() {
        Company companyA = new Company("Tata Power", "REG-A", "a@tata.com", "111", "Mumbai");
        companyA.setId(101L);

        User manager = new User("mgr@smartev.com", "hash", "Manager Suresh", "123", Role.STATION_MANAGER, companyA);
        manager.setId(4L);

        Station stationUnassigned = new Station(companyA, "Tata Whitefield Hub", "ITPL", "Bangalore", "KA", "560066", 12.98, 77.73, 3);
        stationUnassigned.setId(20L);

        StationManagerAssignmentRepository mockAssignmentRepo = mock(StationManagerAssignmentRepository.class);
        when(mockAssignmentRepo.existsByUserIdAndStationId(4L, 20L)).thenReturn(false);

        StationService stationService = new StationService(null, null, null, null, null, null, null, null, null, null, null, null, mockAssignmentRepo);

        UnauthorizedException ex = assertThrows(UnauthorizedException.class, () ->
                stationService.validateCompanyAccess(stationUnassigned, manager)
        );
        assertTrue(ex.getMessage().contains("Station is not assigned to this station manager"));
    }

    @Test
    @DisplayName("Station Manager cannot access station belonging to another company")
    void testStationManagerCannotAccessOtherCompanyStation() {
        Company companyA = new Company("Tata Power", "REG-A", "a@tata.com", "111", "Mumbai");
        companyA.setId(101L);

        Company companyB = new Company("Zeon Charging", "REG-B", "b@zeon.com", "222", "Coimbatore");
        companyB.setId(202L);

        User manager = new User("mgr@smartev.com", "hash", "Manager Suresh", "123", Role.STATION_MANAGER, companyA);
        manager.setId(4L);

        Station zeonStation = new Station(companyB, "Zeon Electronic City", "Hosur Rd", "Bangalore", "KA", "560100", 12.84, 77.66, 3);
        zeonStation.setId(30L);

        StationService stationService = new StationService(null, null, null, null, null, null);

        UnauthorizedException ex = assertThrows(UnauthorizedException.class, () ->
                stationService.validateCompanyAccess(zeonStation, manager)
        );
        assertTrue(ex.getMessage().contains("Station belongs to a different charging company"));
    }

    @Test
    @DisplayName("Station Manager cannot view bookings or sessions outside their assigned stations")
    void testStationManagerCannotViewBookingsOrSessionsOutsideAssignedStations() {
        Company companyA = new Company("Tata Power", "REG-A", "a@tata.com", "111", "Mumbai");
        companyA.setId(101L);

        User manager = new User("mgr@smartev.com", "hash", "Manager Suresh", "123", Role.STATION_MANAGER, companyA);
        manager.setId(4L);

        User driver = new User("driver@smartev.com", "hash", "Driver", "555", Role.DRIVER, null);
        driver.setId(99L);

        Station stationAssigned = new Station(companyA, "Assigned Hub", "100ft Rd", "Bangalore", "KA", "560038", 12.97, 77.64, 2);
        stationAssigned.setId(10L);

        Station stationUnassigned = new Station(companyA, "Unassigned Hub", "Airport Rd", "Bangalore", "KA", "560300", 13.19, 77.70, 2);
        stationUnassigned.setId(20L);

        Charger chargerUnassigned = new Charger(stationUnassigned, "CHG-UNASSIGNED", "DC", "CCS2", 60.0, "OPERATIONAL", "AVAILABLE");
        chargerUnassigned.setId(200L);

        Booking bookingUnassigned = new Booking("BK-UNASSIGNED", driver, null, stationUnassigned, chargerUnassigned,
                LocalDateTime.now(), LocalDateTime.now().plusHours(1), 5.0, BigDecimal.valueOf(150.00));

        ChargingSession sessionUnassigned = new ChargingSession(bookingUnassigned, chargerUnassigned, driver,
                "SESS-UNASSIGNED", LocalDateTime.now(), 20);

        StationManagerAssignmentRepository mockAssignmentRepo = mock(StationManagerAssignmentRepository.class);
        when(mockAssignmentRepo.existsByUserIdAndStationId(4L, 10L)).thenReturn(true);
        when(mockAssignmentRepo.existsByUserIdAndStationId(4L, 20L)).thenReturn(false);

        BookingService bookingService = new BookingService(null, null, null, null, null, null, mockAssignmentRepo, BigDecimal.valueOf(150.00));
        ChargingSessionService sessionService = new ChargingSessionService(null, null, null, null, null, mockAssignmentRepo);

        // Booking access check: unassigned station booking must be denied
        UnauthorizedException bookingEx = assertThrows(UnauthorizedException.class, () ->
                bookingService.validateBookingAccess(bookingUnassigned, manager)
        );
        assertTrue(bookingEx.getMessage().contains("Booking does not belong to an assigned station"));

        // Session access check: unassigned station session must be denied
        UnauthorizedException sessionEx = assertThrows(UnauthorizedException.class, () ->
                sessionService.validateSessionAccess(sessionUnassigned, manager)
        );
        assertTrue(sessionEx.getMessage().contains("Charging session does not belong to an assigned station"));
    }

    @Test
    @DisplayName("Should enforce that only PLATFORM_ADMIN principal possesses administrative directory authority")
    void testOnlyPlatformAdminHasDirectoryAuthority() {
        User adminUser = new User("admin@smartev.com", "hash", "Admin", null, Role.PLATFORM_ADMIN, null);
        adminUser.setId(1L);
        UserPrincipal adminPrincipal = UserPrincipal.create(adminUser);

        User driverUser = new User("driver@smartev.com", "hash", "Driver", null, Role.DRIVER, null);
        driverUser.setId(2L);
        UserPrincipal driverPrincipal = UserPrincipal.create(driverUser);

        User cpoUser = new User("cpo@smartev.com", "hash", "CPO", null, Role.COMPANY_ADMIN, null);
        cpoUser.setId(3L);
        UserPrincipal cpoPrincipal = UserPrincipal.create(cpoUser);

        User mgrUser = new User("mgr@smartev.com", "hash", "Manager", null, Role.STATION_MANAGER, null);
        mgrUser.setId(4L);
        UserPrincipal mgrPrincipal = UserPrincipal.create(mgrUser);

        assertTrue(adminPrincipal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PLATFORM_ADMIN")));
        assertFalse(driverPrincipal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PLATFORM_ADMIN")));
        assertFalse(cpoPrincipal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PLATFORM_ADMIN")));
        assertFalse(mgrPrincipal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PLATFORM_ADMIN")));
    }
}
