package com.manacommunity.api.billing.ledger.repository;

import com.manacommunity.api.billing.ledger.model.GeneralLedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GeneralLedgerEntryRepository extends JpaRepository<GeneralLedgerEntry, Long> {
    List<GeneralLedgerEntry> findByAccountId(Long accountId);
}
