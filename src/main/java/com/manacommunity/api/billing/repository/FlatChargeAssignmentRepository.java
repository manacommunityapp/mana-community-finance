package com.manacommunity.api.billing.repository;

import com.manacommunity.api.billing.model.FlatChargeAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FlatChargeAssignmentRepository extends JpaRepository<FlatChargeAssignment, Long> {
    List<FlatChargeAssignment> findByCommunityIdAndActiveTrue(Long communityId);
    Optional<FlatChargeAssignment> findByCommunityIdAndFlatNumberAndTower(Long communityId, String flatNumber, String tower);
    List<FlatChargeAssignment> findByCommunityIdAndTower(Long communityId, String tower);
}
