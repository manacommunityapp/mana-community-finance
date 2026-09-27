package com.manacommunity.api.billing.repository;

import com.manacommunity.api.billing.model.CommunityReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommunityReceiptRepository extends JpaRepository<CommunityReceipt, Long> {
    Optional<CommunityReceipt> findByReceiptNumber(String receiptNumber);
    Optional<CommunityReceipt> findByTransactionId(Long transactionId);
    List<CommunityReceipt> findByInvoiceId(Long invoiceId);
    List<CommunityReceipt> findByCommunityIdAndFlatNumberAndTower(Long communityId, String flatNumber, String tower);
}
