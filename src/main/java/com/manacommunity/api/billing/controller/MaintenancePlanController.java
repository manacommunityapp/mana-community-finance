package com.manacommunity.api.billing.controller;

import com.manacommunity.api.billing.dto.FlatChargeAssignmentRequest;
import com.manacommunity.api.billing.dto.FlatChargeAssignmentResponse;
import com.manacommunity.api.billing.dto.MaintenancePlanRequest;
import com.manacommunity.api.billing.dto.MaintenancePlanResponse;
import com.manacommunity.api.billing.service.MaintenanceConfigurationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/finance/maintenance-plans")
@RequiredArgsConstructor
public class MaintenancePlanController {

    private final MaintenanceConfigurationService configurationService;

    @GetMapping
    public ResponseEntity<List<MaintenancePlanResponse>> getPlans(
            @RequestParam(required = false, defaultValue = "1") Long communityId) {
        return ResponseEntity.ok(configurationService.getPlans(communityId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MaintenancePlanResponse> getPlanById(@PathVariable Long id) {
        return ResponseEntity.ok(configurationService.getPlanById(id));
    }

    @PostMapping
    public ResponseEntity<MaintenancePlanResponse> createPlan(
            @RequestParam(required = false, defaultValue = "1") Long communityId,
            @Valid @RequestBody MaintenancePlanRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(configurationService.createPlan(communityId, req));
    }

    @GetMapping("/flat-assignments")
    public ResponseEntity<List<FlatChargeAssignmentResponse>> getFlatAssignments(
            @RequestParam(required = false, defaultValue = "1") Long communityId) {
        return ResponseEntity.ok(configurationService.getFlatAssignments(communityId));
    }

    @PostMapping("/flat-assignments")
    public ResponseEntity<FlatChargeAssignmentResponse> assignFlat(
            @RequestParam(required = false, defaultValue = "1") Long communityId,
            @Valid @RequestBody FlatChargeAssignmentRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(configurationService.assignFlat(communityId, req));
    }
}
