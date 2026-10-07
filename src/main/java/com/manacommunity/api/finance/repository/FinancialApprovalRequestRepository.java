package com.manacommunity.api.finance.repository;

import com.manacommunity.api.finance.entity.FinancialApprovalRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FinancialApprovalRequestRepository extends JpaRepository<FinancialApprovalRequest, Long> {
    Page<FinancialApprovalRequest> findByCommunityIdOrderByCreatedAtDesc(Long communityId, Pageable pageable);
    Page<FinancialApprovalRequest> findByCommunityIdAndStatusOrderByCreatedAtDesc(
            Long communityId, FinancialApprovalRequest.ApprovalStatus status, Pageable pageable);
    Optional<FinancialApprovalRequest> findByIdAndCommunityId(Long id, Long communityId);
    Optional<FinancialApprovalRequest> findByCommunityIdAndEntityTypeAndEntityId(
            Long communityId, FinancialApprovalRequest.ApprovalEntityType entityType, Long entityId);
}
