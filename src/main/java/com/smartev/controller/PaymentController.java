package com.smartev.controller;

import com.smartev.dto.PaymentRequest;
import com.smartev.dto.PaymentResponse;
import com.smartev.entity.User;
import com.smartev.exception.ResourceNotFoundException;
import com.smartev.repository.UserRepository;
import com.smartev.security.UserPrincipal;
import com.smartev.service.DemoPaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final DemoPaymentService paymentService;
    private final UserRepository userRepository;

    public PaymentController(DemoPaymentService paymentService, UserRepository userRepository) {
        this.paymentService = paymentService;
        this.userRepository = userRepository;
    }

    /**
     * Submit simulated demo token payment.
     * Supports simulateFailure flag to demonstrate failure handling.
     * Accessible at both /api/payments and /api/payments/demo
     */
    @PostMapping({"", "/demo"})
    public ResponseEntity<PaymentResponse> processDemoPayment(
            @Valid @RequestBody PaymentRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        User driver = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(paymentService.processDemoPayment(request, driver));
    }

    /**
     * View demo payment transactions history.
     */
    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getPayments(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        User currentUser = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(paymentService.getPaymentsForUser(currentUser));
    }
}
