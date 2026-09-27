package com.manacommunity.api.billing.ledger.service;

import com.manacommunity.api.billing.ledger.dto.GeneralLedgerAccountDto;
import com.manacommunity.api.billing.ledger.dto.GeneralLedgerTransactionDto;
import com.manacommunity.api.billing.ledger.dto.TrialBalanceResponse;
import com.manacommunity.api.billing.ledger.enums.AccountType;
import com.manacommunity.api.billing.ledger.enums.EntryType;
import com.manacommunity.api.billing.ledger.model.GeneralLedgerAccount;
import com.manacommunity.api.billing.ledger.model.GeneralLedgerEntry;
import com.manacommunity.api.billing.ledger.model.GeneralLedgerTransaction;
import com.manacommunity.api.billing.ledger.repository.GeneralLedgerAccountRepository;
import com.manacommunity.api.billing.ledger.repository.GeneralLedgerTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeneralLedgerService {

    private final GeneralLedgerAccountRepository accountRepository;
    private final GeneralLedgerTransactionRepository transactionRepository;

    public void initializeStandardAccountsIfMissing(Long communityId) {
        ensureAccount(communityId, "1000", "Bank & Cash Clearing", AccountType.ASSET);
        ensureAccount(communityId, "1100", "Resident Maintenance Receivable", AccountType.ASSET);
        ensureAccount(communityId, "2000", "Resident Advance Payments Wallet", AccountType.LIABILITY);
        ensureAccount(communityId, "2100", "Sinking / Corpus Fund Reserve", AccountType.LIABILITY);
        ensureAccount(communityId, "4000", "Maintenance Fee Revenue", AccountType.REVENUE);
        ensureAccount(communityId, "4100", "Water & Utility Charges Revenue", AccountType.REVENUE);
        ensureAccount(communityId, "4200", "Parking & Facility Fee Revenue", AccountType.REVENUE);
        ensureAccount(communityId, "4300", "Late Fee & Penalty Revenue", AccountType.REVENUE);
    }

    private GeneralLedgerAccount ensureAccount(Long communityId, String code, String name, AccountType type) {
        return accountRepository.findByCommunityIdAndAccountCode(communityId, code)
                .orElseGet(() -> accountRepository.save(GeneralLedgerAccount.builder()
                        .communityId(communityId)
                        .accountCode(code)
                        .accountName(name)
                        .accountType(type)
                        .currentBalance(BigDecimal.ZERO)
                        .isActive(true)
                        .build()));
    }

    @Transactional
    public GeneralLedgerTransaction recordInvoiceIssued(Long communityId, Long invoiceId, String invoiceNumber,
                                                        BigDecimal subtotal, BigDecimal penalty, BigDecimal total) {
        initializeStandardAccountsIfMissing(communityId);
        GeneralLedgerAccount receivable = ensureAccount(communityId, "1100", "Resident Maintenance Receivable", AccountType.ASSET);
        GeneralLedgerAccount maintenanceRev = ensureAccount(communityId, "4000", "Maintenance Fee Revenue", AccountType.REVENUE);

        GeneralLedgerTransaction tx = GeneralLedgerTransaction.builder()
                .communityId(communityId)
                .transactionRef("TX-INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .transactionDate(LocalDate.now())
                .referenceType("INVOICE")
                .referenceId(invoiceId)
                .narration("Invoice issued: " + invoiceNumber)
                .totalAmount(total)
                .build();

        List<GeneralLedgerEntry> entries = new ArrayList<>();
        // Debit Receivable (Asset Increase)
        entries.add(GeneralLedgerEntry.builder()
                .transaction(tx)
                .account(receivable)
                .entryType(EntryType.DEBIT)
                .amount(total)
                .notes("Receivable for invoice " + invoiceNumber)
                .build());

        // Credit Revenue (Income Increase)
        entries.add(GeneralLedgerEntry.builder()
                .transaction(tx)
                .account(maintenanceRev)
                .entryType(EntryType.CREDIT)
                .amount(subtotal)
                .notes("Maintenance revenue for invoice " + invoiceNumber)
                .build());

        if (penalty != null && penalty.compareTo(BigDecimal.ZERO) > 0) {
            GeneralLedgerAccount penaltyRev = ensureAccount(communityId, "4300", "Late Fee & Penalty Revenue", AccountType.REVENUE);
            entries.add(GeneralLedgerEntry.builder()
                    .transaction(tx)
                    .account(penaltyRev)
                    .entryType(EntryType.CREDIT)
                    .amount(penalty)
                    .notes("Late fee penalty revenue")
                    .build());
        }

        tx.setEntries(entries);
        updateAccountBalances(entries);
        return transactionRepository.save(tx);
    }

    @Transactional
    public GeneralLedgerTransaction recordPaymentReceived(Long communityId, Long paymentId, String txRef,
                                                         BigDecimal amount, String paymentMode) {
        initializeStandardAccountsIfMissing(communityId);
        GeneralLedgerAccount bank = ensureAccount(communityId, "1000", "Bank & Cash Clearing", AccountType.ASSET);
        GeneralLedgerAccount receivable = ensureAccount(communityId, "1100", "Resident Maintenance Receivable", AccountType.ASSET);

        GeneralLedgerTransaction tx = GeneralLedgerTransaction.builder()
                .communityId(communityId)
                .transactionRef("TX-PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .transactionDate(LocalDate.now())
                .referenceType("PAYMENT")
                .referenceId(paymentId)
                .narration("Payment received via " + paymentMode + " (Ref: " + txRef + ")")
                .totalAmount(amount)
                .build();

        List<GeneralLedgerEntry> entries = new ArrayList<>();
        // Debit Bank/Cash (Asset Increase)
        entries.add(GeneralLedgerEntry.builder()
                .transaction(tx)
                .account(bank)
                .entryType(EntryType.DEBIT)
                .amount(amount)
                .notes("Bank clearing receipt")
                .build());

        // Credit Receivable (Asset Decrease)
        entries.add(GeneralLedgerEntry.builder()
                .transaction(tx)
                .account(receivable)
                .entryType(EntryType.CREDIT)
                .amount(amount)
                .notes("Receivable liquidation")
                .build());

        tx.setEntries(entries);
        updateAccountBalances(entries);
        return transactionRepository.save(tx);
    }

    private void updateAccountBalances(List<GeneralLedgerEntry> entries) {
        for (GeneralLedgerEntry entry : entries) {
            GeneralLedgerAccount acc = entry.getAccount();
            BigDecimal change = entry.getAmount();
            boolean isDebit = entry.getEntryType() == EntryType.DEBIT;

            // Normal debit balance for Asset & Expense
            if (acc.getAccountType() == AccountType.ASSET || acc.getAccountType() == AccountType.EXPENSE) {
                acc.setCurrentBalance(isDebit ? acc.getCurrentBalance().add(change) : acc.getCurrentBalance().subtract(change));
            } else {
                // Normal credit balance for Liability, Equity, Revenue
                acc.setCurrentBalance(!isDebit ? acc.getCurrentBalance().add(change) : acc.getCurrentBalance().subtract(change));
            }
            accountRepository.save(acc);
        }
    }

    public List<GeneralLedgerAccountDto> getAccounts(Long communityId) {
        initializeStandardAccountsIfMissing(communityId);
        return accountRepository.findByCommunityIdOrderByAccountCodeAsc(communityId).stream()
                .map(this::toAccountDto).toList();
    }

    public Page<GeneralLedgerTransactionDto> getTransactions(Long communityId, Pageable pageable) {
        return transactionRepository.findByCommunityIdOrderByTransactionDateDesc(communityId, pageable)
                .map(this::toTransactionDto);
    }

    public TrialBalanceResponse getTrialBalance(Long communityId) {
        List<GeneralLedgerAccount> accounts = accountRepository.findByCommunityIdOrderByAccountCodeAsc(communityId);
        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;
        List<TrialBalanceResponse.TrialBalanceItem> items = new ArrayList<>();

        for (GeneralLedgerAccount acc : accounts) {
            BigDecimal bal = acc.getCurrentBalance();
            BigDecimal debit = BigDecimal.ZERO;
            BigDecimal credit = BigDecimal.ZERO;

            if (acc.getAccountType() == AccountType.ASSET || acc.getAccountType() == AccountType.EXPENSE) {
                if (bal.compareTo(BigDecimal.ZERO) >= 0) debit = bal;
                else credit = bal.abs();
            } else {
                if (bal.compareTo(BigDecimal.ZERO) >= 0) credit = bal;
                else debit = bal.abs();
            }

            totalDebit = totalDebit.add(debit);
            totalCredit = totalCredit.add(credit);

            items.add(TrialBalanceResponse.TrialBalanceItem.builder()
                    .accountCode(acc.getAccountCode())
                    .accountName(acc.getAccountName())
                    .accountType(acc.getAccountType().name())
                    .debitAmount(debit)
                    .creditAmount(credit)
                    .build());
        }

        return TrialBalanceResponse.builder()
                .communityId(communityId)
                .items(items)
                .totalDebit(totalDebit)
                .totalCredit(totalCredit)
                .isBalanced(totalDebit.compareTo(totalCredit) == 0)
                .build();
    }

    private GeneralLedgerAccountDto toAccountDto(GeneralLedgerAccount a) {
        return GeneralLedgerAccountDto.builder()
                .id(a.getId())
                .communityId(a.getCommunityId())
                .accountCode(a.getAccountCode())
                .accountName(a.getAccountName())
                .accountType(a.getAccountType())
                .currentBalance(a.getCurrentBalance())
                .description(a.getDescription())
                .isActive(a.getIsActive())
                .build();
    }

    private GeneralLedgerTransactionDto toTransactionDto(GeneralLedgerTransaction tx) {
        return GeneralLedgerTransactionDto.builder()
                .id(tx.getId())
                .communityId(tx.getCommunityId())
                .transactionRef(tx.getTransactionRef())
                .transactionDate(tx.getTransactionDate())
                .referenceType(tx.getReferenceType())
                .referenceId(tx.getReferenceId())
                .narration(tx.getNarration())
                .totalAmount(tx.getTotalAmount())
                .createdAt(tx.getCreatedAt())
                .entries(tx.getEntries().stream().map(e -> GeneralLedgerTransactionDto.EntryItemDto.builder()
                        .id(e.getId())
                        .accountId(e.getAccount().getId())
                        .accountCode(e.getAccount().getAccountCode())
                        .accountName(e.getAccount().getAccountName())
                        .entryType(e.getEntryType())
                        .amount(e.getAmount())
                        .notes(e.getNotes())
                        .build()).toList())
                .build();
    }
}
