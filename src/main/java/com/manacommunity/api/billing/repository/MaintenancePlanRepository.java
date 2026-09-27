package com.manacommunity.api.billing.repository;

import com.manacommunity.api.billing.model.MaintenancePlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MaintenancePlanRepository extends JpaRepository<MaintenancePlan, Long> {
    List<MaintenancePlan> findByCommunityIdAndActiveTrue(Long communityId);
    Optional<MaintenancePlan> findFirstByCommunityIdAndActiveTrueOrderByCreatedAtDesc(Long communityId);
}
