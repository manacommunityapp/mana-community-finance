package com.manacommunity.api.billing.controller;

import com.manacommunity.api.billing.dto.*;
import com.manacommunity.api.billing.service.BillingPaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/finance/payments")
@RequiredArgsConstructor
public class BillingPaymentController {

    private final BillingPaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentTransactionResponse> processPayment(
            @RequestParam(required = false, defaultValue = "1") Long communityId,
            @RequestParam(required = false, defaultValue = "1") Long userId,
            @RequestParam(required = false, defaultValue = "Resident") String userName,
            @Valid @RequestBody ProcessPaymentRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.processPayment(communityId, req, userId, userName));
    }

    @PostMapping("/advance/deposit")
    public ResponseEntity<AdvanceBalanceResponse> depositAdvance(
            @RequestParam(required = false, defaultValue = "1") Long communityId,
            @RequestParam(required = false, defaultValue = "1") Long userId,
            @Valid @RequestBody AdvanceDepositRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.depositAdvance(communityId, req, userId));
    }

    @GetMapping("/advance/balance")
    public ResponseEntity<AdvanceBalanceResponse> getAdvanceBalance(
            @RequestParam(required = false, defaultValue = "1") Long communityId,
            @RequestParam String flatNumber,
            @RequestParam String tower) {
        return ResponseEntity.ok(paymentService.getAdvanceBalance(communityId, flatNumber, tower));
    }

    @GetMapping("/receipts/{receiptNumber}")
    public ResponseEntity<ReceiptDto> getReceipt(@PathVariable String receiptNumber) {
        return ResponseEntity.ok(paymentService.getReceiptByNumber(receiptNumber));
    }

    @GetMapping("/receipts/invoice/{invoiceId}")
    public ResponseEntity<List<ReceiptDto>> getReceiptsForInvoice(@PathVariable Long invoiceId) {
        return ResponseEntity.ok(paymentService.getReceiptsForInvoice(invoiceId));
    }

    @GetMapping("/receipts/flat")
    public ResponseEntity<List<ReceiptDto>> getReceiptsForFlat(
            @RequestParam(required = false, defaultValue = "1") Long communityId,
            @RequestParam String flatNumber,
            @RequestParam String tower) {
        return ResponseEntity.ok(paymentService.getReceiptsForFlat(communityId, flatNumber, tower));
    }
}
