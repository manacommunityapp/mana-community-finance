package com.manacommunity.api.finance.service;

import com.manacommunity.api.finance.dto.ApprovalRequestDetailDto;
import com.manacommunity.api.finance.dto.ApprovalRequestDetailDto.SignoffItemDto;
import com.manacommunity.api.finance.dto.CreateApprovalRequestDto;
import com.manacommunity.api.finance.dto.SubmitSignoffRequestDto;
import com.manacommunity.api.finance.entity.FinancialApprovalRequest;
import com.manacommunity.api.finance.entity.FinancialApprovalRequest.ApprovalStatus;
import com.manacommunity.api.finance.entity.FinancialApprovalSignoff;
import com.manacommunity.api.finance.entity.FinancialApprovalSignoff.SignoffDecision;
import com.manacommunity.api.finance.repository.FinancialApprovalRequestRepository;
import com.manacommunity.api.finance.repository.FinancialApprovalSignoffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FinancialApprovalWorkflowService {

    private final FinancialApprovalRequestRepository requestRepository;
    private final FinancialApprovalSignoffRepository signoffRepository;

    @Transactional
    public ApprovalRequestDetailDto createRequest(CreateApprovalRequestDto dto) {
        int required = dto.getRequiredSignoffs() != null ? dto.getRequiredSignoffs() : 2;
        if (dto.getAmount() != null && dto.getAmount().compareTo(BigDecimal.valueOf(50000)) > 0) {
            required = Math.max(required, 3); // High value requires 3 signoffs (President, Treasurer, Secretary)
        }

        FinancialApprovalRequest req = FinancialApprovalRequest.builder()
                .communityId(dto.getCommunityId())
                .entityType(dto.getEntityType())
                .entityId(dto.getEntityId())
                .title(dto.getTitle())
                .amount(dto.getAmount() != null ? dto.getAmount() : BigDecimal.ZERO)
                .requestedBy(dto.getRequestedBy())
                .notes(dto.getNotes())
                .requiredSignoffs(required)
                .currentSignoffs(0)
                .status(ApprovalStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        FinancialApprovalRequest saved = requestRepository.save(req);
        return mapDetail(saved, List.of());
    }

    @Transactional
    public ApprovalRequestDetailDto submitSignoff(Long requestId, Long communityId, SubmitSignoffRequestDto dto) {
        FinancialApprovalRequest req = requestRepository.findByIdAndCommunityId(requestId, communityId)
                .orElseThrow(() -> new IllegalArgumentException("Approval request not found: " + requestId));

        if (req.getStatus() != ApprovalStatus.PENDING) {
            throw new IllegalStateException("Approval request is already " + req.getStatus());
        }

        if (signoffRepository.findByRequestIdAndApproverId(requestId, dto.getApproverId()).isPresent()) {
            throw new IllegalStateException("Approver has already submitted a signoff for this request");
        }

        FinancialApprovalSignoff signoff = FinancialApprovalSignoff.builder()
                .requestId(requestId)
                .approverId(dto.getApproverId())
                .approverName(dto.getApproverName())
                .approverRole(dto.getApproverRole())
                .decision(dto.getDecision())
                .comments(dto.getComments())
                .signedAt(LocalDateTime.now())
                .build();

        signoffRepository.save(signoff);

        if (dto.getDecision() == SignoffDecision.REJECTED) {
            req.setStatus(ApprovalStatus.REJECTED);
            req.setFinalizedAt(LocalDateTime.now());
        } else {
            req.setCurrentSignoffs(req.getCurrentSignoffs() + 1);
            if (req.getCurrentSignoffs() >= req.getRequiredSignoffs()) {
                req.setStatus(ApprovalStatus.APPROVED);
                req.setFinalizedAt(LocalDateTime.now());
            }
        }

        FinancialApprovalRequest updated = requestRepository.save(req);
        List<FinancialApprovalSignoff> allSignoffs = signoffRepository.findByRequestIdOrderBySignedAtAsc(requestId);
        return mapDetail(updated, allSignoffs);
    }

    @Transactional(readOnly = true)
    public Page<FinancialApprovalRequest> getRequests(Long communityId, ApprovalStatus status, Pageable pageable) {
        if (status != null) {
            return requestRepository.findByCommunityIdAndStatusOrderByCreatedAtDesc(communityId, status, pageable);
        }
        return requestRepository.findByCommunityIdOrderByCreatedAtDesc(communityId, pageable);
    }

    @Transactional(readOnly = true)
    public ApprovalRequestDetailDto getRequestDetail(Long requestId, Long communityId) {
        FinancialApprovalRequest req = requestRepository.findByIdAndCommunityId(requestId, communityId)
                .orElseThrow(() -> new IllegalArgumentException("Approval request not found: " + requestId));
        List<FinancialApprovalSignoff> signoffs = signoffRepository.findByRequestIdOrderBySignedAtAsc(requestId);
        return mapDetail(req, signoffs);
    }

    private ApprovalRequestDetailDto mapDetail(FinancialApprovalRequest req, List<FinancialApprovalSignoff> signoffs) {
        return ApprovalRequestDetailDto.builder()
                .id(req.getId())
                .communityId(req.getCommunityId())
                .entityType(req.getEntityType())
                .entityId(req.getEntityId())
                .title(req.getTitle())
                .amount(req.getAmount())
                .requestedBy(req.getRequestedBy())
                .notes(req.getNotes())
                .requiredSignoffs(req.getRequiredSignoffs())
                .currentSignoffs(req.getCurrentSignoffs())
                .status(req.getStatus())
                .createdAt(req.getCreatedAt())
                .finalizedAt(req.getFinalizedAt())
                .signoffs(signoffs.stream().map(s -> SignoffItemDto.builder()
                        .id(s.getId())
                        .approverId(s.getApproverId())
                        .approverName(s.getApproverName())
                        .approverRole(s.getApproverRole())
                        .decision(s.getDecision().name())
                        .comments(s.getComments())
                        .signedAt(s.getSignedAt())
                        .build()).collect(Collectors.toList()))
                .build();
    }
}
