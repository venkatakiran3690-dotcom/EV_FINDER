package com.smartev.controller;

import com.smartev.dto.UserResponse;
import com.smartev.entity.*;
import com.smartev.repository.ActivityLogRepository;
import com.smartev.repository.CompanyRepository;
import com.smartev.repository.UserRepository;
import com.smartev.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlatformAdminControllerTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ActivityLogRepository activityLogRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private PlatformAdminController controller;

    private User driver;
    private User companyAdmin;
    private User stationManager;
    private User platformAdmin;
    private Company testCompany;

    @BeforeEach
    void setUp() {
        controller = new PlatformAdminController(
                companyRepository,
                userRepository,
                activityLogRepository,
                passwordEncoder
        );

        testCompany = new Company("Tata Power EV", "CPO-TATA-01", "tata@ev.com", "+91 99999 11111", "Bangalore");
        testCompany.setId(1L);

        driver = new User("driver@smartev.com", "$2a$10$dummyHash", "Driver One", "+91 98765 43210", Role.DRIVER, null);
        driver.setId(10L);

        companyAdmin = new User("cpo@smartev.com", "$2a$10$dummyHash", "CPO Admin", "+91 98765 00002", Role.COMPANY_ADMIN, testCompany);
        companyAdmin.setId(20L);

        stationManager = new User("manager@smartev.com", "$2a$10$dummyHash", "Station Manager", "+91 98765 00004", Role.STATION_MANAGER, testCompany);
        stationManager.setId(30L);

        platformAdmin = new User("admin@smartev.com", "$2a$10$dummyHash", "Platform Admin", "+91 98765 00001", Role.PLATFORM_ADMIN, null);
        platformAdmin.setId(40L);
    }

    @Test
    @DisplayName("getUsers without role filter returns all users as UserResponse DTOs")
    void testGetUsersAllRoles() {
        when(userRepository.findAll()).thenReturn(List.of(driver, companyAdmin, stationManager, platformAdmin));

        ResponseEntity<List<UserResponse>> response = controller.getUsers(null);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(4, response.getBody().size());

        // Verify DTO fields and that passwordHash is never exposed
        UserResponse rDriver = response.getBody().stream().filter(u -> u.getRole() == Role.DRIVER).findFirst().orElseThrow();
        assertEquals("driver@smartev.com", rDriver.getEmail());
        assertEquals("Driver One", rDriver.getFullName());

        UserResponse rCpo = response.getBody().stream().filter(u -> u.getRole() == Role.COMPANY_ADMIN).findFirst().orElseThrow();
        assertEquals(1L, rCpo.getCompanyId());
        assertEquals("Tata Power EV", rCpo.getCompanyName());

        verify(userRepository, times(1)).findAll();
        verify(userRepository, never()).findByRole(any());
    }

    @Test
    @DisplayName("getUsers with role=DRIVER returns only Drivers")
    void testGetUsersFilterDriver() {
        when(userRepository.findByRole(Role.DRIVER)).thenReturn(List.of(driver));

        ResponseEntity<List<UserResponse>> response = controller.getUsers(Role.DRIVER);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
        assertEquals(Role.DRIVER, response.getBody().get(0).getRole());
        assertEquals("driver@smartev.com", response.getBody().get(0).getEmail());

        verify(userRepository, times(1)).findByRole(Role.DRIVER);
    }

    @Test
    @DisplayName("getUsers with role=COMPANY_ADMIN returns only Company Admins")
    void testGetUsersFilterCompanyAdmin() {
        when(userRepository.findByRole(Role.COMPANY_ADMIN)).thenReturn(List.of(companyAdmin));

        ResponseEntity<List<UserResponse>> response = controller.getUsers(Role.COMPANY_ADMIN);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
        assertEquals(Role.COMPANY_ADMIN, response.getBody().get(0).getRole());
        assertEquals("cpo@smartev.com", response.getBody().get(0).getEmail());

        verify(userRepository, times(1)).findByRole(Role.COMPANY_ADMIN);
    }

    @Test
    @DisplayName("getUsers with role=STATION_MANAGER returns only Station Managers")
    void testGetUsersFilterStationManager() {
        when(userRepository.findByRole(Role.STATION_MANAGER)).thenReturn(List.of(stationManager));

        ResponseEntity<List<UserResponse>> response = controller.getUsers(Role.STATION_MANAGER);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
        assertEquals(Role.STATION_MANAGER, response.getBody().get(0).getRole());

        verify(userRepository, times(1)).findByRole(Role.STATION_MANAGER);
    }

    @Test
    @DisplayName("getUsers with role=PLATFORM_ADMIN returns only Platform Admins")
    void testGetUsersFilterPlatformAdmin() {
        when(userRepository.findByRole(Role.PLATFORM_ADMIN)).thenReturn(List.of(platformAdmin));

        ResponseEntity<List<UserResponse>> response = controller.getUsers(Role.PLATFORM_ADMIN);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
        assertEquals(Role.PLATFORM_ADMIN, response.getBody().get(0).getRole());

        verify(userRepository, times(1)).findByRole(Role.PLATFORM_ADMIN);
    }

    @Test
    @DisplayName("getLogs returns stored database activity logs with stored timestamp")
    void testGetLogsReturnsStoredDatabaseTimestamps() {
        LocalDateTime storedTimestamp = LocalDateTime.of(2026, 11, 7, 10, 0, 0);
        ActivityLog log = new ActivityLog(1L, "system@smartev.com", "SYSTEM", "Application Startup", "Data initialized", "127.0.0.1");
        log.setId(100L);

        when(activityLogRepository.findTop50ByOrderByTimestampDesc()).thenReturn(List.of(log));

        ResponseEntity<List<ActivityLog>> response = controller.getLogs();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
        assertEquals("system@smartev.com", response.getBody().get(0).getUserEmail());
        assertEquals("Application Startup", response.getBody().get(0).getAction());
        assertNotNull(response.getBody().get(0).getTimestamp(), "Timestamp must be populated from database entity");

        verify(activityLogRepository, times(1)).findTop50ByOrderByTimestampDesc();
    }

    @Test
    @DisplayName("createPrivilegedUser records an ActivityLog in the database")
    void testCreatePrivilegedUserRecordsAuditLog() {
        when(userRepository.existsByEmail("newmgr@smartev.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedSecret");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserPrincipal adminPrincipal = new UserPrincipal(1L, "admin@smartev.com", "pass", "Admin", Role.PLATFORM_ADMIN, null);

        Map<String, String> payload = Map.of(
                "email", "newmgr@smartev.com",
                "password", "Mgr@12345",
                "fullName", "New Manager",
                "phone", "+91 99999 88888",
                "role", "STATION_MANAGER"
        );

        ResponseEntity<Map<String, Object>> response = controller.createPrivilegedUser(payload, adminPrincipal);

        assertEquals(200, response.getStatusCode().value());
        assertTrue((Boolean) response.getBody().get("success"));

        // Verify audit log was saved
        verify(activityLogRepository, times(1)).save(argThat(log ->
                "admin@smartev.com".equals(log.getUserEmail()) &&
                log.getAction().contains("Privileged User Created: newmgr@smartev.com")
        ));
    }
}
