package com.manacommunity.api.billing.controller;

import com.manacommunity.api.billing.dto.BillingCycleSummaryResponse;
import com.manacommunity.api.billing.dto.GenerateMonthlyBillsRequest;
import com.manacommunity.api.billing.dto.PenaltyRuleDto;
import com.manacommunity.api.billing.service.BillingCycleService;
import com.manacommunity.api.billing.service.PenaltyCalculationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/finance/billing-cycles")
@RequiredArgsConstructor
public class BillingCycleController {

    private final BillingCycleService billingCycleService;
    private final PenaltyCalculationService penaltyCalculationService;

    @PostMapping("/generate")
    public ResponseEntity<BillingCycleSummaryResponse> generateMonthlyBills(
            @RequestParam(required = false, defaultValue = "1") Long communityId,
            @Valid @RequestBody GenerateMonthlyBillsRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(billingCycleService.generateMonthlyBills(communityId, req));
    }

    @GetMapping("/penalties")
    public ResponseEntity<PenaltyRuleDto> getPenaltyRule(
            @RequestParam(required = false, defaultValue = "1") Long communityId) {
        return ResponseEntity.ok(penaltyCalculationService.getPenaltyRule(communityId));
    }

    @PostMapping("/penalties")
    public ResponseEntity<PenaltyRuleDto> configurePenaltyRule(
            @RequestParam(required = false, defaultValue = "1") Long communityId,
            @Valid @RequestBody PenaltyRuleDto req) {
        return ResponseEntity.ok(penaltyCalculationService.configurePenaltyRule(communityId, req));
    }

    @PostMapping("/penalties/scan")
    public ResponseEntity<String> scanOverduePenalties(
            @RequestParam(required = false, defaultValue = "1") Long communityId) {
        penaltyCalculationService.scanAndApplyPenalties(communityId);
        return ResponseEntity.ok("Overdue penalty scan executed successfully.");
    }
}
