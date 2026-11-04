package com.smartev.service;

import com.smartev.dto.PaymentRequest;
import com.smartev.dto.PaymentResponse;
import com.smartev.entity.*;
import com.smartev.exception.BadRequestException;
import com.smartev.exception.ResourceNotFoundException;
import com.smartev.exception.UnauthorizedException;
import com.smartev.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DemoPaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final ChargerRepository chargerRepository;
    private final NotificationRepository notificationRepository;

    public DemoPaymentService(PaymentRepository paymentRepository,
                              BookingRepository bookingRepository,
                              ChargerRepository chargerRepository,
                              NotificationRepository notificationRepository) {
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
        this.chargerRepository = chargerRepository;
        this.notificationRepository = notificationRepository;
    }

    /**
     * Executes a demo token transaction for an EV charging slot booking.
     * Demonstrates both SUCCESS and FAILURE paths safely without real financial interaction.
     */
    @Transactional
    public PaymentResponse processDemoPayment(PaymentRequest request, User driver) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + request.getBookingId()));

        if (!booking.getDriver().getId().equals(driver.getId()) && driver.getRole() != Role.PLATFORM_ADMIN) {
            throw new UnauthorizedException("Unauthorized: You do not own this booking.");
        }

        if ("CONFIRMED".equalsIgnoreCase(booking.getStatus())) {
            throw new BadRequestException("Booking has already been confirmed and paid.");
        }

        if ("CANCELLED".equalsIgnoreCase(booking.getStatus()) || "EXPIRED".equalsIgnoreCase(booking.getStatus())) {
            throw new BadRequestException("Cannot pay for a " + booking.getStatus() + " booking.");
        }

        String txnRef = "TXN-DEMO-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();

        // Check for simulated failure path
        if (request.isSimulateFailure()) {
            Payment failedPayment = new Payment(
                    booking,
                    driver,
                    txnRef,
                    booking.getTokenAmount(),
                    "DEMO_TOKEN",
                    "FAILED",
                    "Simulated Payment Failure: Demo token authorization rejected by gateway."
            );
            Payment savedFailed = paymentRepository.save(failedPayment);

            Notification notification = new Notification(
                    driver,
                    "Payment Failed: " + booking.getBookingCode(),
                    "Demo token transaction was rejected. You can retry payment to confirm your booking.",
                    "PAYMENT_CONFIRMATION"
            );
            notificationRepository.save(notification);

            return mapPaymentToResponse(savedFailed);
        }

        // Successful Payment Path
        Payment payment = new Payment(
                booking,
                driver,
                txnRef,
                booking.getTokenAmount(),
                "DEMO_TOKEN",
                "SUCCESS",
                request.getPaymentMethodNote() != null ? request.getPaymentMethodNote() : "Demo Token Wallet (Simulated)"
        );
        Payment saved = paymentRepository.save(payment);

        // Confirm booking
        booking.setStatus("CONFIRMED");
        bookingRepository.save(booking);

        // Mark charger as RESERVED
        Charger charger = booking.getCharger();
        if ("AVAILABLE".equalsIgnoreCase(charger.getAvailabilityStatus())) {
            charger.setAvailabilityStatus("RESERVED");
            chargerRepository.save(charger);
        }

        // In-app Notification
        Notification notification = new Notification(
                driver,
                "Booking Confirmed: " + booking.getBookingCode(),
                String.format("Payment of ₹%.2f successful. Slot confirmed at %s (%s).",
                        booking.getTokenAmount(), booking.getStation().getName(), charger.getChargerIdentifier()),
                "PAYMENT_CONFIRMATION"
        );
        notificationRepository.save(notification);

        return mapPaymentToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsForUser(User user) {
        if (user.getRole() == Role.DRIVER) {
            return paymentRepository.findByDriverIdOrderByCreatedAtDesc(user.getId()).stream()
                    .map(this::mapPaymentToResponse)
                    .collect(Collectors.toList());
        } else {
            return paymentRepository.findAll().stream()
                    .map(this::mapPaymentToResponse)
                    .collect(Collectors.toList());
        }
    }

    private PaymentResponse mapPaymentToResponse(Payment payment) {
        PaymentResponse res = new PaymentResponse();
        res.setId(payment.getId());
        res.setBookingId(payment.getBooking().getId());
        res.setBookingCode(payment.getBooking().getBookingCode());
        res.setTransactionReference(payment.getTransactionReference());
        res.setAmount(payment.getAmount());
        res.setStatus(payment.getStatus());
        res.setPaymentType(payment.getPaymentType());
        res.setPaymentMethodNote(payment.getPaymentMethodNote());
        res.setCreatedAt(payment.getCreatedAt());
        return res;
    }
}
