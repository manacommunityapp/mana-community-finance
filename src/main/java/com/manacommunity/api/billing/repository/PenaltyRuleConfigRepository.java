package com.manacommunity.api.billing.repository;

import com.manacommunity.api.billing.model.PenaltyRuleConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PenaltyRuleConfigRepository extends JpaRepository<PenaltyRuleConfig, Long> {
    Optional<PenaltyRuleConfig> findByCommunityIdAndActiveTrue(Long communityId);
}
