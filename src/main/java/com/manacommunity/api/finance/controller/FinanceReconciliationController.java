package com.manacommunity.api.finance.controller;

import com.manacommunity.api.finance.dto.ReconciliationResultDto;
import com.manacommunity.api.finance.dto.ReconciliationStatementItemDto;
import com.manacommunity.api.finance.dto.SettlementRecordRequestDto;
import com.manacommunity.api.finance.entity.GatewaySettlementRecord;
import com.manacommunity.api.finance.entity.ReconciliationBatch;
import com.manacommunity.api.finance.service.FinanceReconciliationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/finance")
@RequiredArgsConstructor
public class FinanceReconciliationController {

    private final FinanceReconciliationService reconciliationService;

    @PostMapping("/reconciliation/run")
    public ResponseEntity<ReconciliationResultDto> runReconciliation(
            @RequestParam Long communityId,
            @RequestParam String gatewayName,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate statementDate,
            @RequestBody List<ReconciliationStatementItemDto> statementItems) {
        return ResponseEntity.ok(reconciliationService.reconcileStatement(communityId, gatewayName, statementDate, statementItems));
    }

    @GetMapping("/reconciliation/batches")
    public ResponseEntity<Page<ReconciliationBatch>> getBatches(
            @RequestParam Long communityId,
            Pageable pageable) {
        return ResponseEntity.ok(reconciliationService.getBatches(communityId, pageable));
    }

    @GetMapping("/reconciliation/batches/{batchId}")
    public ResponseEntity<ReconciliationResultDto> getBatchDetails(
            @PathVariable Long batchId,
            @RequestParam Long communityId) {
        return ResponseEntity.ok(reconciliationService.getBatchDetails(batchId, communityId));
    }

    @PostMapping("/settlements")
    public ResponseEntity<GatewaySettlementRecord> recordSettlement(@RequestBody SettlementRecordRequestDto dto) {
        return ResponseEntity.ok(reconciliationService.recordSettlement(dto));
    }

    @GetMapping("/settlements")
    public ResponseEntity<Page<GatewaySettlementRecord>> getSettlements(
            @RequestParam Long communityId,
            Pageable pageable) {
        return ResponseEntity.ok(reconciliationService.getSettlements(communityId, pageable));
    }
}
