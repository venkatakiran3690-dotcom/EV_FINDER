package com.smartev.service;

import com.smartev.dto.PaymentRequest;
import com.smartev.dto.PaymentResponse;
import com.smartev.entity.*;
import com.smartev.exception.BadRequestException;
import com.smartev.repository.BookingRepository;
import com.smartev.repository.ChargerRepository;
import com.smartev.repository.NotificationRepository;
import com.smartev.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Demo Token Payment Processing Tests")
class DemoPaymentServiceTest {

    private PaymentRepository paymentRepository;
    private BookingRepository bookingRepository;
    private ChargerRepository chargerRepository;
    private NotificationRepository notificationRepository;
    private DemoPaymentService paymentService;

    private User driver;
    private Station station;
    private Charger charger;
    private Booking booking;

    @BeforeEach
    void setUp() {
        paymentRepository = mock(PaymentRepository.class);
        bookingRepository = mock(BookingRepository.class);
        chargerRepository = mock(ChargerRepository.class);
        notificationRepository = mock(NotificationRepository.class);

        paymentService = new DemoPaymentService(paymentRepository, bookingRepository, chargerRepository, notificationRepository);

        driver = new User("driver@smartev.com", "hash", "Kiran", "9876", Role.DRIVER, null);
        driver.setId(1L);

        Company company = new Company("Tata Power", "REG-01", "c@tata.com", "123", "Mumbai");
        company.setId(10L);

        station = new Station(company, "Indiranagar Hub", "100ft Rd", "Bangalore", "KA", "560038", 12.97, 77.64, 2);
        station.setId(20L);

        charger = new Charger(station, "CHG-01", "DC", "CCS2", 60.0, "OPERATIONAL", "AVAILABLE");
        charger.setId(30L);

        booking = new Booking("BK-PAY-001", driver, null, station, charger,
                LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(2), 5.0, BigDecimal.valueOf(150.00));
        booking.setId(100L);
        booking.setStatus("PENDING_PAYMENT");

        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> {
            Payment p = i.getArgument(0);
            p.setId(500L);
            return p;
        });
    }

    @Test
    @DisplayName("Should successfully process demo token payment and confirm slot reservation")
    void testSuccessfulPaymentFlow() {
        PaymentRequest request = new PaymentRequest(100L, false, "Demo Wallet FastPay");

        PaymentResponse response = paymentService.processDemoPayment(request, driver);

        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertEquals("CONFIRMED", booking.getStatus());
        assertEquals("RESERVED", charger.getAvailabilityStatus());
        assertTrue(response.getTransactionReference().startsWith("TXN-DEMO-"));

        verify(bookingRepository, times(1)).save(booking);
        verify(chargerRepository, times(1)).save(charger);
    }

    @Test
    @DisplayName("Should handle simulated failure path and leave booking in pending status")
    void testSimulatedFailurePaymentFlow() {
        PaymentRequest request = new PaymentRequest(100L, true, "Simulated Fail Test");

        PaymentResponse response = paymentService.processDemoPayment(request, driver);

        assertNotNull(response);
        assertEquals("FAILED", response.getStatus());
        assertEquals("PENDING_PAYMENT", booking.getStatus()); // Stays pending
        assertEquals("AVAILABLE", charger.getAvailabilityStatus()); // Charger not reserved
    }

    @Test
    @DisplayName("Should reject payment if booking is already confirmed")
    void testRejectDoublePayment() {
        booking.setStatus("CONFIRMED");

        PaymentRequest request = new PaymentRequest(100L, false, "Demo Wallet");

        assertThrows(BadRequestException.class, () ->
                paymentService.processDemoPayment(request, driver)
        );
    }
}
