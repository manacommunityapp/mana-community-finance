package com.manacommunity.api.billing.controller;

import com.manacommunity.api.billing.dto.AdminBillingDashboardResponse;
import com.manacommunity.api.billing.dto.ResidentDuesSummaryResponse;
import com.manacommunity.api.billing.service.BillingDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/finance/dashboard")
@RequiredArgsConstructor
public class BillingDashboardController {

    private final BillingDashboardService dashboardService;

    @GetMapping("/admin")
    public ResponseEntity<AdminBillingDashboardResponse> getAdminDashboard(
            @RequestParam(required = false, defaultValue = "1") Long communityId,
            @RequestParam(required = false) String period) {
        return ResponseEntity.ok(dashboardService.getAdminDashboard(communityId, period));
    }

    @GetMapping("/resident")
    public ResponseEntity<ResidentDuesSummaryResponse> getResidentDuesSummary(
            @RequestParam(required = false, defaultValue = "1") Long communityId,
            @RequestParam String flatNumber,
            @RequestParam String tower) {
        return ResponseEntity.ok(dashboardService.getResidentDuesSummary(communityId, flatNumber, tower));
    }
}
