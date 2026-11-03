package com.smartev.service;

import com.smartev.dto.BookingRequest;
import com.smartev.dto.BookingResponse;
import com.smartev.entity.*;
import com.smartev.exception.BadRequestException;
import com.smartev.exception.SlotConflictException;
import com.smartev.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Slot Pre-Booking & Concurrency Conflict Tests")
class BookingServiceTest {

    private BookingRepository bookingRepository;
    private StationRepository stationRepository;
    private ChargerRepository chargerRepository;
    private VehicleRepository vehicleRepository;
    private NotificationRepository notificationRepository;
    private GeofenceService geofenceService;
    private BookingService bookingService;

    private Station station;
    private Charger charger;
    private User driver;

    @BeforeEach
    void setUp() {
        bookingRepository = mock(BookingRepository.class);
        stationRepository = mock(StationRepository.class);
        chargerRepository = mock(ChargerRepository.class);
        vehicleRepository = mock(VehicleRepository.class);
        notificationRepository = mock(NotificationRepository.class);
        geofenceService = mock(GeofenceService.class);

        bookingService = new BookingService(
                bookingRepository,
                stationRepository,
                chargerRepository,
                vehicleRepository,
                notificationRepository,
                geofenceService,
                BigDecimal.valueOf(150.00)
        );

        Company company = new Company("Tata Power", "REG-01", "c@tata.com", "123", "Mumbai");
        company.setId(1L);

        station = new Station(company, "Indiranagar Hub", "100ft Rd", "Bangalore", "KA", "560038", 12.9784, 77.6408, 2);
        station.setId(10L);

        charger = new Charger(station, "CHG-01", "DC", "CCS2", 60.0, "OPERATIONAL", "AVAILABLE");
        charger.setId(100L);

        driver = new User("driver@smartev.com", "hash", "Kiran", "9876", Role.DRIVER, null);
        driver.setId(5L);

        when(stationRepository.findById(10L)).thenReturn(Optional.of(station));
        when(chargerRepository.findById(100L)).thenReturn(Optional.of(charger));
        when(geofenceService.validateGeofence(anyDouble(), anyDouble(), anyDouble(), anyDouble())).thenReturn(5.2);
    }

    @Test
    @DisplayName("Should successfully create a booking when charger is operational and slot is free")
    void testSuccessfulBooking() {
        LocalDateTime futureStart = LocalDateTime.now().plusHours(2);
        BookingRequest request = new BookingRequest();
        request.setStationId(10L);
        request.setChargerId(100L);
        request.setStartTime(futureStart);
        request.setDurationMinutes(60);
        request.setDriverLatitude(12.9500);
        request.setDriverLongitude(77.6000);

        when(bookingRepository.findConflictingBookings(eq(100L), any(), any())).thenReturn(Collections.emptyList());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> {
            Booking b = i.getArgument(0);
            b.setId(1L);
            return b;
        });

        BookingResponse response = bookingService.createBooking(request, driver);

        assertNotNull(response);
        assertEquals("PENDING_PAYMENT", response.getStatus());
        assertEquals(new BigDecimal("150.00"), response.getTokenAmount());
        assertEquals(5.2, response.getCalculatedDistanceKm());
    }

    @Test
    @DisplayName("Should reject booking if charger is already reserved in an overlapping time window")
    void testRejectOverlappingBooking() {
        LocalDateTime start = LocalDateTime.now().plusHours(2);
        BookingRequest request = new BookingRequest();
        request.setStationId(10L);
        request.setChargerId(100L);
        request.setStartTime(start);
        request.setDurationMinutes(60);
        request.setDriverLatitude(12.9500);
        request.setDriverLongitude(77.6000);

        Booking existingBooking = new Booking("BK-EXISTING", driver, null, station, charger,
                start.plusMinutes(15), start.plusMinutes(75), 5.0, BigDecimal.valueOf(150.00));

        when(bookingRepository.findConflictingBookings(eq(100L), any(), any()))
                .thenReturn(List.of(existingBooking));

        assertThrows(SlotConflictException.class, () -> bookingService.createBooking(request, driver));
    }

    @Test
    @DisplayName("Should reject booking if charger is OUT_OF_SERVICE")
    void testRejectOutOfServiceCharger() {
        charger.setOperationalStatus("OUT_OF_SERVICE");

        BookingRequest request = new BookingRequest();
        request.setStationId(10L);
        request.setChargerId(100L);
        request.setStartTime(LocalDateTime.now().plusHours(2));
        request.setDurationMinutes(60);
        request.setDriverLatitude(12.9500);
        request.setDriverLongitude(77.6000);

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                bookingService.createBooking(request, driver)
        );
        assertTrue(ex.getMessage().contains("OUT OF SERVICE"));
    }

    @Test
    @DisplayName("Should reject booking if start time is in the past")
    void testRejectPastBookingStartTime() {
        BookingRequest request = new BookingRequest();
        request.setStationId(10L);
        request.setChargerId(100L);
        request.setStartTime(LocalDateTime.now().minusHours(1)); // 1 hour ago
        request.setDurationMinutes(60);
        request.setDriverLatitude(12.9500);
        request.setDriverLongitude(77.6000);

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                bookingService.createBooking(request, driver)
        );
        assertTrue(ex.getMessage().contains("cannot be in the past"));
    }

    @Test
    @DisplayName("Should permit cancellation only if scheduled start time is at least 30 minutes away")
    void testCancellationWindowRule() {
        // Case 1: Start time is 10 minutes from now -> Should be rejected
        Booking nearBooking = new Booking("BK-NEAR", driver, null, station, charger,
                LocalDateTime.now().plusMinutes(10), LocalDateTime.now().plusMinutes(70), 5.0, BigDecimal.valueOf(150.00));
        nearBooking.setId(201L);
        nearBooking.setStatus("CONFIRMED");

        when(bookingRepository.findById(201L)).thenReturn(Optional.of(nearBooking));

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                bookingService.cancelBooking(201L, driver)
        );
        assertTrue(ex.getMessage().contains("at least 30 minutes before"));

        // Case 2: Start time is 2 hours from now -> Should succeed
        Booking farBooking = new Booking("BK-FAR", driver, null, station, charger,
                LocalDateTime.now().plusHours(2), LocalDateTime.now().plusHours(3), 5.0, BigDecimal.valueOf(150.00));
        farBooking.setId(202L);
        farBooking.setStatus("CONFIRMED");

        when(bookingRepository.findById(202L)).thenReturn(Optional.of(farBooking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArgument(0));

        BookingResponse cancelResponse = bookingService.cancelBooking(202L, driver);
        assertEquals("CANCELLED", cancelResponse.getStatus());
    }
}
