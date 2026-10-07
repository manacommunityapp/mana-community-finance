package com.manacommunity.api.finance.repository;

import com.manacommunity.api.finance.entity.CommunityRefund;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommunityRefundRepository extends JpaRepository<CommunityRefund, Long> {

    Page<CommunityRefund> findByCommunityId(Long communityId, Pageable pageable);

    List<CommunityRefund> findByCommunityIdAndResidentId(Long communityId, Long residentId);

    List<CommunityRefund> findByPaymentId(Long paymentId);

    List<CommunityRefund> findByInvoiceId(Long invoiceId);

    Optional<CommunityRefund> findByIdAndCommunityId(Long id, Long communityId);
}
