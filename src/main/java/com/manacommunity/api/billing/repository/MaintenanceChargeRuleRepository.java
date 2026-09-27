package com.manacommunity.api.billing.repository;

import com.manacommunity.api.billing.model.MaintenanceChargeRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaintenanceChargeRuleRepository extends JpaRepository<MaintenanceChargeRule, Long> {
    List<MaintenanceChargeRule> findByPlanId(Long planId);
    void deleteByPlanId(Long planId);
}
