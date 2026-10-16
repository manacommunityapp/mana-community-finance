package com.manacommunity.api.finance.repository;

import com.manacommunity.api.finance.entity.GatewaySettlementRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface GatewaySettlementRecordRepository extends JpaRepository<GatewaySettlementRecord, Long> {
    Page<GatewaySettlementRecord> findByCommunityIdOrderBySettlementDateDesc(Long communityId, Pageable pageable);
    Optional<GatewaySettlementRecord> findByIdAndCommunityId(Long id, Long communityId);

    @Query("SELECT COALESCE(SUM(g.grossAmount), 0) FROM GatewaySettlementRecord g WHERE g.communityId = :cid AND g.settlementDate BETWEEN :from AND :to")
    BigDecimal sumGrossAmountBetween(@Param("cid") Long communityId, @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT COALESCE(SUM(g.netPayoutAmount), 0) FROM GatewaySettlementRecord g WHERE g.communityId = :cid AND g.settlementDate BETWEEN :from AND :to AND g.payoutStatus = com.manacommunity.api.finance.entity.GatewaySettlementRecord$PayoutStatus.SETTLED")
    BigDecimal sumSettledNetAmountBetween(@Param("cid") Long communityId, @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT COALESCE(SUM(g.gatewayFee + g.gstOnFee), 0) FROM GatewaySettlementRecord g WHERE g.communityId = :cid AND g.settlementDate BETWEEN :from AND :to")
    BigDecimal sumGatewayFeesBetween(@Param("cid") Long communityId, @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT FUNCTION('DATE_FORMAT', g.settlementDate, '%Y-%m'), COALESCE(SUM(g.grossAmount), 0) FROM GatewaySettlementRecord g WHERE g.communityId = :cid AND g.settlementDate BETWEEN :from AND :to GROUP BY FUNCTION('DATE_FORMAT', g.settlementDate, '%Y-%m') ORDER BY 1")
    List<Object[]> monthlyGrossAmounts(@Param("cid") Long communityId, @Param("from") LocalDate from, @Param("to") LocalDate to);
}