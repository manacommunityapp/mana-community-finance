package com.manacommunity.api.billing.ledger.controller;

import com.manacommunity.api.billing.ledger.dto.GeneralLedgerAccountDto;
import com.manacommunity.api.billing.ledger.dto.GeneralLedgerTransactionDto;
import com.manacommunity.api.billing.ledger.dto.TrialBalanceResponse;
import com.manacommunity.api.billing.ledger.service.GeneralLedgerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/finance/ledger")
@RequiredArgsConstructor
public class GeneralLedgerController {

    private final GeneralLedgerService ledgerService;

    @GetMapping("/accounts")
    public ResponseEntity<List<GeneralLedgerAccountDto>> getAccounts(
            @RequestParam(defaultValue = "1") Long communityId) {
        return ResponseEntity.ok(ledgerService.getAccounts(communityId));
    }

    @GetMapping("/transactions")
    public ResponseEntity<Page<GeneralLedgerTransactionDto>> getTransactions(
            @RequestParam(defaultValue = "1") Long communityId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ledgerService.getTransactions(communityId, PageRequest.of(page, size)));
    }

    @GetMapping("/trial-balance")
    public ResponseEntity<TrialBalanceResponse> getTrialBalance(
            @RequestParam(defaultValue = "1") Long communityId) {
        return ResponseEntity.ok(ledgerService.getTrialBalance(communityId));
    }
}
