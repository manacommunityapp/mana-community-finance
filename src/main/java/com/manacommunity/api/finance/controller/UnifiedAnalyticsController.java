package com.manacommunity.api.finance.controller;

import com.manacommunity.api.finance.dto.UnifiedAnalyticsResponse;
import com.manacommunity.api.finance.service.UnifiedAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Unified Business and Finance Analytics endpoint.
 * <p>
 * Provides community administrators a consolidated view of:
 * <ul>
 *   <li>Maintenance collections (billing periods, collection rates, tower-wise breakdown)</li>
 *   <li>Vendor spending (payments, business expenses, top vendors by spend)</li>
 *   <li>Event budgets (budget vs actual spend by category)</li>
 *   <li>Commerce revenue (settled gateway amounts, sales orders, monthly trend)</li>
 *   <li>Outstanding payments (overdue invoices, unpaid vendor bills)</li>
 *   <li>Financial health score (composite 0–100 score with grade and highlights)</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/finance/analytics")
@RequiredArgsConstructor
public class UnifiedAnalyticsController {

    private final UnifiedAnalyticsService analyticsService;

    /**
     * Returns a unified financial analytics snapshot for the given community and date range.
     *
     * <p>Defaults: {@code from} = first day of the current month, {@code to} = today.
     *
     * @param communityId the community to scope all analytics to
     * @param from        optional period start (ISO date, e.g. 2026-01-01)
     * @param to          optional period end (ISO date, e.g. 2026-10-31)
     */
    @GetMapping("/unified")
    @PreAuthorize("hasAnyAuthority('View Finance Reports', 'Admin')")
    public ResponseEntity<UnifiedAnalyticsResponse> getUnifiedAnalytics(
            @RequestParam Long communityId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        LocalDate periodFrom = from != null ? from : LocalDate.now().withDayOfMonth(1);
        LocalDate periodTo   = to   != null ? to   : LocalDate.now();

        if (periodFrom.isAfter(periodTo)) {
            return ResponseEntity.badRequest().build();
        }

        UnifiedAnalyticsResponse response = analyticsService.getUnifiedAnalytics(communityId, periodFrom, periodTo);
        return ResponseEntity.ok(response);
    }
}
