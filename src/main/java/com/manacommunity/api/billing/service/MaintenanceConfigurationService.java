package com.manacommunity.api.billing.service;

import com.manacommunity.api.billing.dto.*;
import com.manacommunity.api.billing.model.*;
import com.manacommunity.api.billing.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MaintenanceConfigurationService {

    private final MaintenancePlanRepository planRepository;
    private final MaintenanceChargeRuleRepository chargeRuleRepository;
    private final FlatChargeAssignmentRepository assignmentRepository;

    public List<MaintenancePlanResponse> getPlans(Long communityId) {
        return planRepository.findByCommunityIdAndActiveTrue(communityId)
                .stream().map(this::mapPlan).collect(Collectors.toList());
    }

    public MaintenancePlanResponse getPlanById(Long id) {
        MaintenancePlan plan = planRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Plan not found: " + id));
        return mapPlan(plan);
    }

    @Transactional
    public MaintenancePlanResponse createPlan(Long communityId, MaintenancePlanRequest req) {
        MaintenancePlan plan = MaintenancePlan.builder()
                .communityId(communityId)
                .name(req.getName())
                .calculationType(req.getCalculationType())
                .ratePerSqFt(req.getRatePerSqFt())
                .fixedAmountPerFlat(req.getFixedAmountPerFlat())
                .effectiveFrom(req.getEffectiveFrom())
                .effectiveTo(req.getEffectiveTo())
                .active(req.getActive() != null ? req.getActive() : true)
                .build();

        MaintenancePlan saved = planRepository.save(plan);

        if (req.getChargeRules() != null) {
            for (ChargeRuleDto ruleDto : req.getChargeRules()) {
                MaintenanceChargeRule rule = MaintenanceChargeRule.builder()
                        .planId(saved.getId())
                        .componentType(ruleDto.getComponentType())
                        .description(ruleDto.getDescription())
                        .rate(ruleDto.getRate())
                        .isMandatory(ruleDto.getIsMandatory() != null ? ruleDto.getIsMandatory() : true)
                        .flatType(ruleDto.getFlatType())
                        .tower(ruleDto.getTower())
                        .build();
                chargeRuleRepository.save(rule);
            }
        }

        return mapPlan(saved);
    }

    // Flat assignments
    public List<FlatChargeAssignmentResponse> getFlatAssignments(Long communityId) {
        return assignmentRepository.findByCommunityIdAndActiveTrue(communityId)
                .stream().map(this::mapAssignment).collect(Collectors.toList());
    }

    @Transactional
    public FlatChargeAssignmentResponse assignFlat(Long communityId, FlatChargeAssignmentRequest req) {
        FlatChargeAssignment assignment = assignmentRepository
                .findByCommunityIdAndFlatNumberAndTower(communityId, req.getFlatNumber(), req.getTower())
                .orElse(FlatChargeAssignment.builder()
                        .communityId(communityId)
                        .flatNumber(req.getFlatNumber())
                        .tower(req.getTower())
                        .build());

        assignment.setFlatId(req.getFlatId());
        assignment.setAreaSqFt(req.getAreaSqFt());
        assignment.setFlatType(req.getFlatType());
        assignment.setPlanId(req.getPlanId());
        assignment.setOwnerUserId(req.getOwnerUserId());
        assignment.setOwnerName(req.getOwnerName());
        assignment.setTenantUserId(req.getTenantUserId());
        assignment.setTenantName(req.getTenantName());
        assignment.setActive(true);

        return mapAssignment(assignmentRepository.save(assignment));
    }

    private MaintenancePlanResponse mapPlan(MaintenancePlan p) {
        List<ChargeRuleDto> rules = chargeRuleRepository.findByPlanId(p.getId())
                .stream().map(r -> ChargeRuleDto.builder()
                        .id(r.getId())
                        .planId(r.getPlanId())
                        .componentType(r.getComponentType())
                        .description(r.getDescription())
                        .rate(r.getRate())
                        .isMandatory(r.getIsMandatory())
                        .flatType(r.getFlatType())
                        .tower(r.getTower())
                        .build()).collect(Collectors.toList());

        return MaintenancePlanResponse.builder()
                .id(p.getId())
                .communityId(p.getCommunityId())
                .name(p.getName())
                .calculationType(p.getCalculationType())
                .ratePerSqFt(p.getRatePerSqFt())
                .fixedAmountPerFlat(p.getFixedAmountPerFlat())
                .effectiveFrom(p.getEffectiveFrom())
                .effectiveTo(p.getEffectiveTo())
                .active(p.getActive())
                .chargeRules(rules)
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    private FlatChargeAssignmentResponse mapAssignment(FlatChargeAssignment a) {
        String planName = a.getPlanId() != null
                ? planRepository.findById(a.getPlanId()).map(MaintenancePlan::getName).orElse(null)
                : null;
        return FlatChargeAssignmentResponse.builder()
                .id(a.getId())
                .communityId(a.getCommunityId())
                .flatId(a.getFlatId())
                .flatNumber(a.getFlatNumber())
                .tower(a.getTower())
                .areaSqFt(a.getAreaSqFt())
                .flatType(a.getFlatType())
                .planId(a.getPlanId())
                .planName(planName)
                .ownerUserId(a.getOwnerUserId())
                .ownerName(a.getOwnerName())
                .tenantUserId(a.getTenantUserId())
                .tenantName(a.getTenantName())
                .active(a.getActive())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
