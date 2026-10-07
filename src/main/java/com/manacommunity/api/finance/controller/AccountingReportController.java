package com.manacommunity.api.finance.controller;

import com.manacommunity.api.finance.dto.AgingReportDto;
import com.manacommunity.api.finance.dto.BalanceSheetDto;
import com.manacommunity.api.finance.dto.CashFlowSummaryDto;
import com.manacommunity.api.finance.dto.IncomeExpenseStatementDto;
import com.manacommunity.api.finance.service.AccountingReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/finance/reports")
@RequiredArgsConstructor
public class AccountingReportController {

    private final AccountingReportService reportService;

    @GetMapping("/income-expense")
    public ResponseEntity<IncomeExpenseStatementDto> getIncomeExpense(
            @RequestParam Long communityId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        LocalDate start = startDate != null ? startDate : LocalDate.now().withDayOfMonth(1);
        LocalDate end = endDate != null ? endDate : LocalDate.now();
        return ResponseEntity.ok(reportService.generateIncomeExpenseStatement(communityId, start, end));
    }

    @GetMapping("/balance-sheet")
    public ResponseEntity<BalanceSheetDto> getBalanceSheet(
            @RequestParam Long communityId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate) {
        LocalDate asOf = asOfDate != null ? asOfDate : LocalDate.now();
        return ResponseEntity.ok(reportService.generateBalanceSheet(communityId, asOf));
    }

    @GetMapping("/aging")
    public ResponseEntity<AgingReportDto> getAgingReport(
            @RequestParam Long communityId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate) {
        LocalDate asOf = asOfDate != null ? asOfDate : LocalDate.now();
        return ResponseEntity.ok(reportService.generateAgingReport(communityId, asOf));
    }

    @GetMapping("/cash-flow")
    public ResponseEntity<CashFlowSummaryDto> getCashFlow(
            @RequestParam Long communityId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        LocalDate start = startDate != null ? startDate : LocalDate.now().withDayOfMonth(1);
        LocalDate end = endDate != null ? endDate : LocalDate.now();
        return ResponseEntity.ok(reportService.generateCashFlowSummary(communityId, start, end));
    }
}
