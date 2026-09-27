package com.manacommunity.api.billing.ledger.repository;

import com.manacommunity.api.billing.ledger.model.GeneralLedgerAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GeneralLedgerAccountRepository extends JpaRepository<GeneralLedgerAccount, Long> {
    List<GeneralLedgerAccount> findByCommunityIdOrderByAccountCodeAsc(Long communityId);
    Optional<GeneralLedgerAccount> findByCommunityIdAndAccountCode(Long communityId, String accountCode);
}
