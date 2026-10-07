package com.manacommunity.api.finance.repository;

import com.manacommunity.api.finance.entity.GatewaySettlementRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GatewaySettlementRecordRepository extends JpaRepository<GatewaySettlementRecord, Long> {
    Page<GatewaySettlementRecord> findByCommunityIdOrderBySettlementDateDesc(Long communityId, Pageable pageable);
    Optional<GatewaySettlementRecord> findByIdAndCommunityId(Long id, Long communityId);
}
