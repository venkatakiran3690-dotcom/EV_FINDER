package com.smartev.controller;

import com.smartev.dto.BillResponse;
import com.smartev.entity.User;
import com.smartev.exception.ResourceNotFoundException;
import com.smartev.repository.UserRepository;
import com.smartev.security.UserPrincipal;
import com.smartev.service.BillingService;
import com.smartev.service.PdfInvoiceService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bills")
public class BillingController {

    private final BillingService billingService;
    private final PdfInvoiceService pdfInvoiceService;
    private final UserRepository userRepository;

    public BillingController(BillingService billingService,
                             PdfInvoiceService pdfInvoiceService,
                             UserRepository userRepository) {
        this.billingService = billingService;
        this.pdfInvoiceService = pdfInvoiceService;
        this.userRepository = userRepository;
    }

    /**
     * List bills and invoices for the authenticated user.
     */
    @GetMapping
    public ResponseEntity<List<BillResponse>> getBills(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        User currentUser = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(billingService.getBillsForUser(currentUser));
    }

    /**
     * Get a specific bill by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<BillResponse> getBillById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        User currentUser = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(billingService.getBillById(id, currentUser));
    }

    /**
     * Download authorized server-side generated PDF invoice.
     */
    @GetMapping("/{id}/invoice")
    public ResponseEntity<byte[]> downloadInvoicePdf(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        User currentUser = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        byte[] pdfBytes = pdfInvoiceService.generateInvoicePdf(id, currentUser);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "EV_Invoice_" + id + ".pdf");
        headers.setContentLength(pdfBytes.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}
