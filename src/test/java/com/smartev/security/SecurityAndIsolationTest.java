package com.smartev.security;

import com.smartev.entity.Company;
import com.smartev.entity.Role;
import com.smartev.entity.Station;
import com.smartev.entity.User;
import com.smartev.exception.UnauthorizedException;
import com.smartev.service.StationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

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
}
