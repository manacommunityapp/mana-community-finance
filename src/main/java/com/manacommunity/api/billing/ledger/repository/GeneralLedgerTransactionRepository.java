package com.manacommunity.api.billing.ledger.repository;

import com.manacommunity.api.billing.ledger.model.GeneralLedgerTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface GeneralLedgerTransactionRepository extends JpaRepository<GeneralLedgerTransaction, Long> {
    Page<GeneralLedgerTransaction> findByCommunityIdOrderByTransactionDateDesc(Long communityId, Pageable pageable);
    List<GeneralLedgerTransaction> findByCommunityIdAndTransactionDateBetween(Long communityId, LocalDate start, LocalDate end);
}
