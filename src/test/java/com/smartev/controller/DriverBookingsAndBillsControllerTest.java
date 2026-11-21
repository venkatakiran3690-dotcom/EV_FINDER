package com.smartev.controller;

import com.smartev.dto.BillResponse;
import com.smartev.dto.BookingResponse;
import com.smartev.entity.Role;
import com.smartev.entity.User;
import com.smartev.exception.UnauthorizedException;
import com.smartev.repository.UserRepository;
import com.smartev.security.UserPrincipal;
import com.smartev.service.BillingService;
import com.smartev.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Driver Dashboard Bookings and Bills Controller, Security & Route Tests")
class DriverBookingsAndBillsControllerTest {

    @Mock
    private BookingService bookingService;

    @Mock
    private BillingService billingService;

    @Mock
    private UserRepository userRepository;

    private BookingController bookingController;
    private BillingController billingController;

    private MockMvc bookingMockMvc;
    private MockMvc billingMockMvc;

    private User driverWithData;
    private User driverWithoutData;
    private User otherDriver;
    private UserPrincipal activePrincipal;

    private BookingResponse demoBooking;
    private BillResponse demoBill;

    @BeforeEach
    void setUp() {
        bookingController = new BookingController(bookingService, userRepository);
        billingController = new BillingController(billingService, null, userRepository);

        driverWithData = new User("driver@smartev.com", "$2a$10$hash", "Venkata Kiran", "+91 98765 43210", Role.DRIVER, null);
        driverWithData.setId(10L);

        driverWithoutData = new User("driver2@smartev.com", "$2a$10$hash", "Ananya Reddy", "+91 98765 43211", Role.DRIVER, null);
        driverWithoutData.setId(20L);

        otherDriver = new User("other@smartev.com", "$2a$10$hash", "Other Driver", "+91 98765 99999", Role.DRIVER, null);
        otherDriver.setId(99L);

        demoBooking = new BookingResponse();
        demoBooking.setId(101L);
        demoBooking.setBookingCode("BK-TEST-01");
        demoBooking.setStationName("Tata Indiranagar Hub");
        demoBooking.setChargerIdentifier("TC-IND-01");
        demoBooking.setStatus("CONFIRMED");
        demoBooking.setTokenAmount(new BigDecimal("150.00"));
        demoBooking.setStartTime(LocalDateTime.of(2026, 11, 15, 10, 0));
        demoBooking.setEndTime(LocalDateTime.of(2026, 11, 15, 11, 0));

        demoBill = new BillResponse();
        demoBill.setId(201L);
        demoBill.setInvoiceNumber("INV-2026-001");
        demoBill.setStationName("Tata Indiranagar Hub");
        demoBill.setEnergyConsumedKwh(24.5);
        demoBill.setTotalAmount(new BigDecimal("514.50"));
        demoBill.setPaymentStatus("PAID");
        demoBill.setCreatedAt(LocalDateTime.of(2026, 11, 10, 14, 30));

        HandlerMethodArgumentResolver principalResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.getParameterType().isAssignableFrom(UserPrincipal.class);
            }

            @Override
            public Object resolveArgument(MethodParameter parameter,
                                          ModelAndViewContainer mavContainer,
                                          NativeWebRequest webRequest,
                                          WebDataBinderFactory binderFactory) {
                return activePrincipal;
            }
        };

        bookingMockMvc = MockMvcBuilders.standaloneSetup(bookingController)
                .setCustomArgumentResolvers(principalResolver)
                .build();

        billingMockMvc = MockMvcBuilders.standaloneSetup(billingController)
                .setCustomArgumentResolvers(principalResolver)
                .build();
    }

    // =========================================================================
    // 1. DRIVER WITH DATA TESTS (BOOKINGS & BILLS)
    // =========================================================================

    @Test
    @DisplayName("Driver with data: GET /api/bookings returns 200 OK with booking list and correct JSON fields")
    void testGetBookingsDriverWithData() throws Exception {
        activePrincipal = UserPrincipal.create(driverWithData);
        when(userRepository.findById(10L)).thenReturn(Optional.of(driverWithData));
        when(bookingService.getBookingsForUser(driverWithData)).thenReturn(List.of(demoBooking));

        // 1. Controller method direct assertion
        ResponseEntity<List<BookingResponse>> response = bookingController.getBookings(activePrincipal);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("BK-TEST-01", response.getBody().get(0).getBookingCode());
        assertEquals(new BigDecimal("150.00"), response.getBody().get(0).getTokenAmount());
        assertEquals(new BigDecimal("150.00"), response.getBody().get(0).getBookingFee());

        // 2. MockMvc HTTP route assertion
        bookingMockMvc.perform(get("/api/bookings").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].bookingCode", is("BK-TEST-01")))
                .andExpect(jsonPath("$[0].stationName", is("Tata Indiranagar Hub")))
                .andExpect(jsonPath("$[0].status", is("CONFIRMED")))
                .andExpect(jsonPath("$[0].tokenAmount", is(150.00)))
                .andExpect(jsonPath("$[0].bookingFee", is(150.00)));
    }

    @Test
    @DisplayName("Driver with data: GET /api/bookings/my alias endpoint returns 200 OK")
    void testGetBookingsMyAliasDriverWithData() throws Exception {
        activePrincipal = UserPrincipal.create(driverWithData);
        when(userRepository.findById(10L)).thenReturn(Optional.of(driverWithData));
        when(bookingService.getBookingsForUser(driverWithData)).thenReturn(List.of(demoBooking));

        bookingMockMvc.perform(get("/api/bookings/my").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].bookingCode", is("BK-TEST-01")));
    }

    @Test
    @DisplayName("Driver with data: GET /api/bills returns 200 OK with bill list and correct JSON fields")
    void testGetBillsDriverWithData() throws Exception {
        activePrincipal = UserPrincipal.create(driverWithData);
        when(userRepository.findById(10L)).thenReturn(Optional.of(driverWithData));
        when(billingService.getBillsForUser(driverWithData)).thenReturn(List.of(demoBill));

        // 1. Controller method direct assertion
        ResponseEntity<List<BillResponse>> response = billingController.getBills(activePrincipal);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("INV-2026-001", response.getBody().get(0).getInvoiceNumber());
        assertEquals(24.5, response.getBody().get(0).getEnergyConsumedKwh());
        assertEquals(24.5, response.getBody().get(0).getTotalUnitsKwh());

        // 2. MockMvc HTTP route assertion
        billingMockMvc.perform(get("/api/bills").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].invoiceNumber", is("INV-2026-001")))
                .andExpect(jsonPath("$[0].stationName", is("Tata Indiranagar Hub")))
                .andExpect(jsonPath("$[0].paymentStatus", is("PAID")))
                .andExpect(jsonPath("$[0].energyConsumedKwh", is(24.5)))
                .andExpect(jsonPath("$[0].totalUnitsKwh", is(24.5)))
                .andExpect(jsonPath("$[0].totalAmount", is(514.50)));
    }

    @Test
    @DisplayName("Driver with data: GET /api/bills/my alias endpoint returns 200 OK")
    void testGetBillsMyAliasDriverWithData() throws Exception {
        activePrincipal = UserPrincipal.create(driverWithData);
        when(userRepository.findById(10L)).thenReturn(Optional.of(driverWithData));
        when(billingService.getBillsForUser(driverWithData)).thenReturn(List.of(demoBill));

        billingMockMvc.perform(get("/api/bills/my").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].invoiceNumber", is("INV-2026-001")));
    }

    // =========================================================================
    // 2. DRIVER WITH NO DATA TESTS (EMPTY STATE)
    // =========================================================================

    @Test
    @DisplayName("Driver without data: GET /api/bookings returns 200 OK with empty list []")
    void testGetBookingsDriverWithNoData() throws Exception {
        activePrincipal = UserPrincipal.create(driverWithoutData);
        when(userRepository.findById(20L)).thenReturn(Optional.of(driverWithoutData));
        when(bookingService.getBookingsForUser(driverWithoutData)).thenReturn(Collections.emptyList());

        // 1. Controller method direct assertion
        ResponseEntity<List<BookingResponse>> response = bookingController.getBookings(activePrincipal);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());

        // 2. MockMvc HTTP route assertion
        bookingMockMvc.perform(get("/api/bookings").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        // 3. /my alias route assertion
        bookingMockMvc.perform(get("/api/bookings/my").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Driver without data: GET /api/bills returns 200 OK with empty list []")
    void testGetBillsDriverWithNoData() throws Exception {
        activePrincipal = UserPrincipal.create(driverWithoutData);
        when(userRepository.findById(20L)).thenReturn(Optional.of(driverWithoutData));
        when(billingService.getBillsForUser(driverWithoutData)).thenReturn(Collections.emptyList());

        // 1. Controller method direct assertion
        ResponseEntity<List<BillResponse>> response = billingController.getBills(activePrincipal);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());

        // 2. MockMvc HTTP route assertion
        billingMockMvc.perform(get("/api/bills").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        // 3. /my alias route assertion
        billingMockMvc.perform(get("/api/bills/my").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // =========================================================================
    // 3. SECURITY & CROSS-DRIVER DATA ISOLATION TESTS
    // =========================================================================

    @Test
    @DisplayName("Security: Driver cannot view another driver's booking by ID")
    void testDriverCannotAccessAnotherDriversBooking() {
        activePrincipal = UserPrincipal.create(driverWithoutData);
        when(userRepository.findById(20L)).thenReturn(Optional.of(driverWithoutData));
        when(bookingService.getBookingById(eq(999L), eq(driverWithoutData)))
                .thenThrow(new UnauthorizedException("You are not authorized to view this booking."));

        UnauthorizedException ex = assertThrows(UnauthorizedException.class, () ->
                bookingController.getBookingById(999L, activePrincipal)
        );
        assertTrue(ex.getMessage().contains("You are not authorized to view this booking"));
        verify(bookingService, times(1)).getBookingById(999L, driverWithoutData);
    }

    @Test
    @DisplayName("Security: Driver cannot view another driver's bill by ID")
    void testDriverCannotAccessAnotherDriversBill() {
        activePrincipal = UserPrincipal.create(driverWithoutData);
        when(userRepository.findById(20L)).thenReturn(Optional.of(driverWithoutData));
        when(billingService.getBillById(eq(888L), eq(driverWithoutData)))
                .thenThrow(new UnauthorizedException("Unauthorized: You do not own this bill."));

        UnauthorizedException ex = assertThrows(UnauthorizedException.class, () ->
                billingController.getBillById(888L, activePrincipal)
        );
        assertTrue(ex.getMessage().contains("You do not own this bill"));
        verify(billingService, times(1)).getBillById(888L, driverWithoutData);
    }
}
