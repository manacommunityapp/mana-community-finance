package com.manacommunity.api.finance.controller;

import com.manacommunity.api.finance.dto.ApprovalRequestDetailDto;
import com.manacommunity.api.finance.dto.CreateApprovalRequestDto;
import com.manacommunity.api.finance.dto.SubmitSignoffRequestDto;
import com.manacommunity.api.finance.entity.FinancialApprovalRequest;
import com.manacommunity.api.finance.entity.FinancialApprovalRequest.ApprovalStatus;
import com.manacommunity.api.finance.service.FinancialApprovalWorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/finance/approvals")
@RequiredArgsConstructor
public class FinancialApprovalController {

    private final FinancialApprovalWorkflowService approvalService;

    @PostMapping
    public ResponseEntity<ApprovalRequestDetailDto> createApprovalRequest(@RequestBody CreateApprovalRequestDto dto) {
        return ResponseEntity.ok(approvalService.createRequest(dto));
    }

    @PostMapping("/{requestId}/signoff")
    public ResponseEntity<ApprovalRequestDetailDto> submitSignoff(
            @PathVariable Long requestId,
            @RequestParam Long communityId,
            @RequestBody SubmitSignoffRequestDto dto) {
        return ResponseEntity.ok(approvalService.submitSignoff(requestId, communityId, dto));
    }

    @GetMapping
    public ResponseEntity<Page<FinancialApprovalRequest>> getRequests(
            @RequestParam Long communityId,
            @RequestParam(required = false) ApprovalStatus status,
            Pageable pageable) {
        return ResponseEntity.ok(approvalService.getRequests(communityId, status, pageable));
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<ApprovalRequestDetailDto> getRequestDetail(
            @PathVariable Long requestId,
            @RequestParam Long communityId) {
        return ResponseEntity.ok(approvalService.getRequestDetail(requestId, communityId));
    }
}
