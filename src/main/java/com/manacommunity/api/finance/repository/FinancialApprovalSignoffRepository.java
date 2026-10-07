package com.manacommunity.api.finance.repository;

import com.manacommunity.api.finance.entity.FinancialApprovalSignoff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FinancialApprovalSignoffRepository extends JpaRepository<FinancialApprovalSignoff, Long> {
    List<FinancialApprovalSignoff> findByRequestIdOrderBySignedAtAsc(Long requestId);
    Optional<FinancialApprovalSignoff> findByRequestIdAndApproverId(Long requestId, Long approverId);
}
