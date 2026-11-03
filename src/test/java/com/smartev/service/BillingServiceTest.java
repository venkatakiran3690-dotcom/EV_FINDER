package com.smartev.service;

import com.smartev.entity.*;
import com.smartev.repository.BillRepository;
import com.smartev.repository.NotificationRepository;
import com.smartev.repository.TariffRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Tariff & Billing Financial Precision Tests")
class BillingServiceTest {

    private BillRepository billRepository;
    private TariffRepository tariffRepository;
    private NotificationRepository notificationRepository;
    private BillingService billingService;

    @BeforeEach
    void setUp() {
        billRepository = mock(BillRepository.class);
        tariffRepository = mock(TariffRepository.class);
        notificationRepository = mock(NotificationRepository.class);
        billingService = new BillingService(billRepository, tariffRepository, notificationRepository);
    }

    @Test
    @DisplayName("Should correctly calculate Energy Cost, Service Charge, GST Tax and Total Amount using BigDecimal")
    void testAccurateBillingCalculation() {
        // Arrange
        Company company = new Company("Tata Power", "REG-01", "c@tata.com", "123", "Mumbai");
        company.setId(1L);

        Station station = new Station(company, "Indiranagar Hub", "100ft Rd", "Bangalore", "KA", "560038", 12.97, 77.64, 2);
        station.setId(10L);

        Charger charger = new Charger(station, "CHG-01", "DC", "CCS2", 60.0, "OPERATIONAL", "AVAILABLE");
        charger.setId(100L);

        User driver = new User("driver@smartev.com", "hash", "Kiran", "9876", Role.DRIVER, null);
        driver.setId(5L);

        Booking booking = new Booking("BK-001", driver, null, station, charger,
                LocalDateTime.now().minusHours(1), LocalDateTime.now(), 5.0, BigDecimal.valueOf(150.00));
        booking.setId(50L);

        ChargingSession session = new ChargingSession(booking, charger, driver, "SESS-001",
                LocalDateTime.now().minusHours(1), 20);
        session.setId(500L);
        session.setEnergyConsumedKwh(30.0); // 30.0 kWh consumed

        Tariff tariff = new Tariff(company, station, "DC",
                BigDecimal.valueOf(20.00), // Rs. 20.00 / kWh
                BigDecimal.valueOf(40.00), // Rs. 40.00 Service charge
                BigDecimal.valueOf(18.00)  // 18.00% GST
        );

        when(billRepository.existsBySessionId(session.getId())).thenReturn(false);
        when(tariffRepository.findApplicableTariffs(eq(1L), eq(10L), eq("DC"))).thenReturn(List.of(tariff));
        when(billRepository.save(any(Bill.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        Bill bill = billingService.generateBillForSession(session);

        // Assert
        // Expected Math:
        // Energy Cost = 30.0 * 20.00 = 600.00
        // Subtotal = 600.00 + 40.00 = 640.00
        // Tax Amount = 640.00 * 18 / 100 = 115.20
        // Total Amount = 640.00 + 115.20 = 755.20
        assertNotNull(bill);
        assertEquals(new BigDecimal("600.00"), bill.getEnergyCost());
        assertEquals(new BigDecimal("40.00"), bill.getServiceCharge());
        assertEquals(new BigDecimal("115.20"), bill.getTaxAmount());
        assertEquals(new BigDecimal("755.20"), bill.getTotalAmount());
        assertEquals("PAID", bill.getPaymentStatus());
    }

    @Test
    @DisplayName("Should prevent duplicate bills for the same completed charging session")
    void testPreventDuplicateBills() {
        ChargingSession session = new ChargingSession();
        session.setId(999L);

        Bill existingBill = new Bill();
        existingBill.setId(1L);
        existingBill.setInvoiceNumber("INV-EXISTING");

        when(billRepository.existsBySessionId(999L)).thenReturn(true);
        when(billRepository.findBySessionId(999L)).thenReturn(Optional.of(existingBill));

        Bill result = billingService.generateBillForSession(session);

        assertNotNull(result);
        assertEquals("INV-EXISTING", result.getInvoiceNumber());
        verify(billRepository, never()).save(any());
    }
}
