package com.manacommunity.api.billing.repository;

import com.manacommunity.api.billing.model.CommunityPaymentTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommunityPaymentTransactionRepository extends JpaRepository<CommunityPaymentTransaction, Long> {
    Optional<CommunityPaymentTransaction> findByTransactionRef(String transactionRef);
    List<CommunityPaymentTransaction> findByInvoiceId(Long invoiceId);
    Page<CommunityPaymentTransaction> findByCommunityId(Long communityId, Pageable pageable);
    List<CommunityPaymentTransaction> findByCommunityId(Long communityId);
    List<CommunityPaymentTransaction> findByCommunityIdAndFlatNumberAndTower(Long communityId, String flatNumber, String tower);
}
