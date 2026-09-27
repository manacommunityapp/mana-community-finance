package com.manacommunity.api.billing.controller;

import com.manacommunity.api.billing.dto.CommunityInvoiceDto;
import com.manacommunity.api.billing.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/finance/invoices")
@RequiredArgsConstructor
public class CommunityInvoiceController {

    private final InvoiceService invoiceService;

    @GetMapping("/{invoiceNumber}")
    public ResponseEntity<CommunityInvoiceDto> getInvoice(@PathVariable String invoiceNumber) {
        return ResponseEntity.ok(invoiceService.getInvoiceByNumber(invoiceNumber));
    }

    @GetMapping("/period/{billingPeriod}")
    public ResponseEntity<Page<CommunityInvoiceDto>> getInvoicesByPeriod(
            @PathVariable String billingPeriod,
            @RequestParam(required = false, defaultValue = "1") Long communityId,
            @PageableDefault(size = 50) Pageable pageable) {
        return ResponseEntity.ok(invoiceService.getInvoicesForPeriod(communityId, billingPeriod, pageable));
    }

    @GetMapping("/flat")
    public ResponseEntity<Page<CommunityInvoiceDto>> getInvoicesByFlat(
            @RequestParam String flatNumber,
            @RequestParam String tower,
            @RequestParam(required = false, defaultValue = "1") Long communityId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(invoiceService.getInvoicesForFlat(communityId, flatNumber, tower, pageable));
    }

    @PostMapping("/{invoiceId}/adjust")
    public ResponseEntity<CommunityInvoiceDto> applyAdjustment(
            @PathVariable Long invoiceId,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false, defaultValue = "Admin Adjustment") String reason) {
        return ResponseEntity.ok(invoiceService.applyAdjustment(invoiceId, amount, reason));
    }
}
