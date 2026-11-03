package com.smartev.service;

import com.smartev.dto.BillResponse;
import com.smartev.entity.*;
import com.smartev.exception.BadRequestException;
import com.smartev.exception.ResourceNotFoundException;
import com.smartev.exception.UnauthorizedException;
import com.smartev.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BillingService {

    private final BillRepository billRepository;
    private final TariffRepository tariffRepository;
    private final NotificationRepository notificationRepository;

    public BillingService(BillRepository billRepository,
                          TariffRepository tariffRepository,
                          NotificationRepository notificationRepository) {
        this.billRepository = billRepository;
        this.tariffRepository = tariffRepository;
        this.notificationRepository = notificationRepository;
    }

    /**
     * Generates a transparent, immutable bill upon completion of an EV charging session.
     * Prevents duplicate invoices for the same session.
     */
    @Transactional
    public Bill generateBillForSession(ChargingSession session) {
        if (billRepository.existsBySessionId(session.getId())) {
            return billRepository.findBySessionId(session.getId()).orElseThrow();
        }

        Station station = session.getCharger().getStation();
        Company company = station.getCompany();
        Charger charger = session.getCharger();

        // 1. Fetch applicable tariff
        List<Tariff> tariffs = tariffRepository.findApplicableTariffs(company.getId(), station.getId(), charger.getChargerType());
        BigDecimal pricePerKwh;
        BigDecimal serviceCharge;
        BigDecimal taxRatePct;

        if (!tariffs.isEmpty()) {
            Tariff tariff = tariffs.get(0);
            pricePerKwh = tariff.getPricePerKwh().setScale(2, RoundingMode.HALF_UP);
            serviceCharge = tariff.getServiceCharge().setScale(2, RoundingMode.HALF_UP);
            taxRatePct = tariff.getTaxPercentage().setScale(2, RoundingMode.HALF_UP);
        } else {
            // Default regulated demo tariff
            pricePerKwh = BigDecimal.valueOf(18.50).setScale(2, RoundingMode.HALF_UP);
            serviceCharge = BigDecimal.valueOf(30.00).setScale(2, RoundingMode.HALF_UP);
            taxRatePct = BigDecimal.valueOf(18.00).setScale(2, RoundingMode.HALF_UP); // 18% GST
        }

        // 2. Strict BigDecimal Monetary Computations
        // Energy Cost = Energy Consumed in kWh * Price per kWh
        BigDecimal energyConsumed = BigDecimal.valueOf(session.getEnergyConsumedKwh()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal energyCost = energyConsumed.multiply(pricePerKwh).setScale(2, RoundingMode.HALF_UP);

        // Subtotal = Energy Cost + Service Charge
        BigDecimal subtotal = energyCost.add(serviceCharge).setScale(2, RoundingMode.HALF_UP);

        // Tax Amount = Subtotal * Tax Percentage / 100
        BigDecimal taxAmount = subtotal.multiply(taxRatePct)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        // Final Amount = Subtotal + Tax Amount
        BigDecimal finalAmount = subtotal.add(taxAmount).setScale(2, RoundingMode.HALF_UP);

        // 3. Unique Invoice Number
        String invoiceNumber = "INV-" + LocalDateTime.now().getYear() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Bill bill = new Bill(
                invoiceNumber,
                session,
                session.getBooking(),
                session.getDriver(),
                company,
                station,
                charger,
                session.getEnergyConsumedKwh(),
                pricePerKwh,
                energyCost,
                serviceCharge,
                taxRatePct,
                taxAmount,
                finalAmount,
                "PAID" // Simulated payment settled
        );

        Bill saved = billRepository.save(bill);

        // Notify Driver
        Notification notification = new Notification(
                session.getDriver(),
                "Invoice Generated: " + invoiceNumber,
                String.format("Charging session complete. Total energy: %.2f kWh. Total billed: ₹%.2f.",
                        session.getEnergyConsumedKwh(), finalAmount),
                "STATUS_UPDATE"
        );
        notificationRepository.save(notification);

        return saved;
    }

    @Transactional(readOnly = true)
    public BillResponse getBillById(Long billId, User currentUser) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with ID: " + billId));

        validateBillAccess(bill, currentUser);
        return mapBillToResponse(bill);
    }

    @Transactional(readOnly = true)
    public List<BillResponse> getBillsForUser(User user) {
        if (user.getRole() == Role.DRIVER) {
            return billRepository.findByDriverIdOrderByCreatedAtDesc(user.getId()).stream()
                    .map(this::mapBillToResponse)
                    .collect(Collectors.toList());
        } else if (user.getRole() == Role.COMPANY_ADMIN) {
            if (user.getCompany() == null) return List.of();
            return billRepository.findByCompanyIdOrderByCreatedAtDesc(user.getCompany().getId()).stream()
                    .map(this::mapBillToResponse)
                    .collect(Collectors.toList());
        } else {
            return billRepository.findAll().stream()
                    .map(this::mapBillToResponse)
                    .collect(Collectors.toList());
        }
    }

    public void validateBillAccess(Bill bill, User user) {
        if (user.getRole() == Role.PLATFORM_ADMIN) return;

        if (user.getRole() == Role.DRIVER) {
            if (!bill.getDriver().getId().equals(user.getId())) {
                throw new UnauthorizedException("Unauthorized: You do not own this bill.");
            }
        } else if (user.getRole() == Role.COMPANY_ADMIN) {
            if (user.getCompany() == null || !user.getCompany().getId().equals(bill.getCompany().getId())) {
                throw new UnauthorizedException("Unauthorized: Bill belongs to another charging company.");
            }
        }
    }

    public BillResponse mapBillToResponse(Bill bill) {
        BillResponse res = new BillResponse();
        res.setId(bill.getId());
        res.setInvoiceNumber(bill.getInvoiceNumber());
        res.setSessionId(bill.getSession().getId());
        res.setSessionCode(bill.getSession().getSessionCode());
        res.setBookingId(bill.getBooking().getId());
        res.setBookingCode(bill.getBooking().getBookingCode());
        res.setDriverId(bill.getDriver().getId());
        res.setDriverName(bill.getDriver().getFullName());
        res.setDriverEmail(bill.getDriver().getEmail());
        res.setCompanyId(bill.getCompany().getId());
        res.setCompanyName(bill.getCompany().getName());
        res.setStationId(bill.getStation().getId());
        res.setStationName(bill.getStation().getName());
        res.setStationAddress(bill.getStation().getAddress() + ", " + bill.getStation().getCity());
        res.setChargerId(bill.getCharger().getId());
        res.setChargerIdentifier(bill.getCharger().getChargerIdentifier());
        res.setChargerType(bill.getCharger().getChargerType());
        res.setEnergyConsumedKwh(bill.getEnergyConsumedKwh());
        res.setPricePerKwh(bill.getPricePerKwh());
        res.setEnergyCost(bill.getEnergyCost());
        res.setServiceCharge(bill.getServiceCharge());
        res.setTaxRatePct(bill.getTaxRatePct());
        res.setTaxAmount(bill.getTaxAmount());
        res.setTotalAmount(bill.getTotalAmount());
        res.setPaymentStatus(bill.getPaymentStatus());
        res.setCreatedAt(bill.getCreatedAt());
        return res;
    }
}
