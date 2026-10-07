package com.manacommunity.api.billing.repository;

import com.manacommunity.api.billing.enums.InvoiceStatus;
import com.manacommunity.api.billing.model.CommunityInvoice;
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
public interface CommunityInvoiceRepository extends JpaRepository<CommunityInvoice, Long> {

    Optional<CommunityInvoice> findByInvoiceNumber(String invoiceNumber);

    Optional<CommunityInvoice> findByCommunityIdAndFlatNumberAndTowerAndBillingPeriod(
            Long communityId, String flatNumber, String tower, String billingPeriod);

    Page<CommunityInvoice> findByCommunityIdAndBillingPeriod(Long communityId, String billingPeriod, Pageable pageable);

    Page<CommunityInvoice> findByCommunityIdAndFlatNumberAndTower(Long communityId, String flatNumber, String tower, Pageable pageable);

    List<CommunityInvoice> findByCommunityIdAndFlatNumberAndTowerAndStatusIn(
            Long communityId, String flatNumber, String tower, List<InvoiceStatus> statuses);

    Page<CommunityInvoice> findByCommunityId(Long communityId, Pageable pageable);

    List<CommunityInvoice> findByCommunityId(Long communityId);

    List<CommunityInvoice> findByCommunityIdAndStatusAndDueDateBefore(
            Long communityId, InvoiceStatus status, LocalDate date);

    @Query("SELECT SUM(i.totalAmount) FROM CommunityInvoice i WHERE i.communityId = :communityId AND i.billingPeriod = :period")
    BigDecimal sumTotalBilledByPeriod(@Param("communityId") Long communityId, @Param("period") String period);

    @Query("SELECT SUM(i.paidAmount) FROM CommunityInvoice i WHERE i.communityId = :communityId AND i.billingPeriod = :period")
    BigDecimal sumTotalPaidByPeriod(@Param("communityId") Long communityId, @Param("period") String period);

    @Query("SELECT SUM(i.outstandingAmount) FROM CommunityInvoice i WHERE i.communityId = :communityId AND i.billingPeriod = :period")
    BigDecimal sumTotalOutstandingByPeriod(@Param("communityId") Long communityId, @Param("period") String period);

    @Query("SELECT i.tower, SUM(i.paidAmount), SUM(i.totalAmount) FROM CommunityInvoice i " +
           "WHERE i.communityId = :communityId AND i.billingPeriod = :period GROUP BY i.tower")
    List<Object[]> getTowerCollectionsByPeriod(@Param("communityId") Long communityId, @Param("period") String period);
}
