package com.manacommunity.api.finance.controller;

import com.manacommunity.api.finance.dto.RefundRequestDto;
import com.manacommunity.api.finance.dto.RefundResponseDto;
import com.manacommunity.api.finance.service.FinanceRefundService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/finance/refunds")
@RequiredArgsConstructor
public class FinanceRefundController {

    private final FinanceRefundService refundService;

    @PostMapping
    public ResponseEntity<RefundResponseDto> requestRefund(@RequestBody RefundRequestDto dto) {
        return ResponseEntity.ok(refundService.requestRefund(dto));
    }

    @GetMapping
    public ResponseEntity<Page<RefundResponseDto>> getRefunds(
            @RequestParam Long communityId,
            Pageable pageable) {
        return ResponseEntity.ok(refundService.getRefundsByCommunity(communityId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RefundResponseDto> getRefund(
            @PathVariable Long id,
            @RequestParam Long communityId) {
        return ResponseEntity.ok(refundService.getRefundById(id, communityId));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<RefundResponseDto> approveRefund(
            @PathVariable Long id,
            @RequestParam Long communityId,
            @RequestParam Long approvedBy) {
        return ResponseEntity.ok(refundService.approveRefund(id, communityId, approvedBy));
    }

    @PostMapping("/{id}/process")
    public ResponseEntity<RefundResponseDto> processRefund(
            @PathVariable Long id,
            @RequestParam Long communityId,
            @RequestParam String transactionRef,
            @RequestParam(required = false) String remarks) {
        return ResponseEntity.ok(refundService.processRefund(id, communityId, transactionRef, remarks));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<RefundResponseDto> rejectRefund(
            @PathVariable Long id,
            @RequestParam Long communityId,
            @RequestParam(required = false) String remarks) {
        return ResponseEntity.ok(refundService.rejectRefund(id, communityId, remarks));
    }
}
