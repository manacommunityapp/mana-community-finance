package com.manacommunity.api.finance.repository;

import com.manacommunity.api.finance.entity.ReconciliationBatch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ReconciliationBatchRepository extends JpaRepository<ReconciliationBatch, Long> {
    Page<ReconciliationBatch> findByCommunityIdOrderByProcessedAtDesc(Long communityId, Pageable pageable);
    Optional<ReconciliationBatch> findByIdAndCommunityId(Long id, Long communityId);
    Optional<ReconciliationBatch> findByCommunityIdAndGatewayNameAndStatementDate(Long communityId, String gatewayName, LocalDate statementDate);
}
