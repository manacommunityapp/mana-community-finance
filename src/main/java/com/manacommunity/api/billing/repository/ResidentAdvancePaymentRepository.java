package com.manacommunity.api.billing.repository;

import com.manacommunity.api.billing.model.ResidentAdvancePayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResidentAdvancePaymentRepository extends JpaRepository<ResidentAdvancePayment, Long> {
    Optional<ResidentAdvancePayment> findByCommunityIdAndFlatNumberAndTower(Long communityId, String flatNumber, String tower);
    List<ResidentAdvancePayment> findByCommunityId(Long communityId);
}
