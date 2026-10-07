package com.manacommunity.api.finance.service;

import com.manacommunity.api.finance.dto.ApprovalRequestDetailDto;
import com.manacommunity.api.finance.dto.CreateApprovalRequestDto;
import com.manacommunity.api.finance.dto.SubmitSignoffRequestDto;
import com.manacommunity.api.finance.entity.FinancialApprovalRequest;
import com.manacommunity.api.finance.entity.FinancialApprovalRequest.ApprovalEntityType;
import com.manacommunity.api.finance.entity.FinancialApprovalRequest.ApprovalStatus;
import com.manacommunity.api.finance.entity.FinancialApprovalSignoff;
import com.manacommunity.api.finance.entity.FinancialApprovalSignoff.SignoffDecision;
import com.manacommunity.api.finance.repository.FinancialApprovalRequestRepository;
import com.manacommunity.api.finance.repository.FinancialApprovalSignoffRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinancialApprovalWorkflowServiceTest {

    @Mock
    private FinancialApprovalRequestRepository requestRepository;

    @Mock
    private FinancialApprovalSignoffRepository signoffRepository;

    @InjectMocks
    private FinancialApprovalWorkflowService approvalService;

    @Test
    void testCreateRequest_SetsPendingStatus() {
        CreateApprovalRequestDto dto = CreateApprovalRequestDto.builder()
                .communityId(1L)
                .entityType(ApprovalEntityType.EXPENSE)
                .entityId(200L)
                .title("Generator Diesel Refill")
                .amount(BigDecimal.valueOf(25000.00))
                .requestedBy(10L)
                .build();

        when(requestRepository.save(any(FinancialApprovalRequest.class))).thenAnswer(i -> {
            FinancialApprovalRequest r = i.getArgument(0);
            r.setId(1L);
            return r;
        });

        ApprovalRequestDetailDto detail = approvalService.createRequest(dto);

        assertNotNull(detail);
        assertEquals(ApprovalStatus.PENDING, detail.getStatus());
        assertEquals(2, detail.getRequiredSignoffs());
        assertEquals(0, detail.getCurrentSignoffs());
    }

    @Test
    void testSubmitSignoff_QuorumApproval() {
        FinancialApprovalRequest req = FinancialApprovalRequest.builder()
                .id(1L)
                .communityId(1L)
                .status(ApprovalStatus.PENDING)
                .requiredSignoffs(2)
                .currentSignoffs(1)
                .build();

        when(requestRepository.findByIdAndCommunityId(1L, 1L)).thenReturn(Optional.of(req));
        when(signoffRepository.findByRequestIdAndApproverId(1L, 22L)).thenReturn(Optional.empty());
        when(requestRepository.save(any(FinancialApprovalRequest.class))).thenAnswer(i -> i.getArgument(0));

        SubmitSignoffRequestDto signoffDto = SubmitSignoffRequestDto.builder()
                .approverId(22L)
                .approverName("Treasurer John")
                .approverRole("TREASURER")
                .decision(SignoffDecision.APPROVED)
                .comments("Verified invoices and budget headroom")
                .build();

        ApprovalRequestDetailDto result = approvalService.submitSignoff(1L, 1L, signoffDto);

        assertNotNull(result);
        assertEquals(ApprovalStatus.APPROVED, result.getStatus());
        assertEquals(2, result.getCurrentSignoffs());
        verify(signoffRepository).save(any(FinancialApprovalSignoff.class));
    }

    @Test
    void testSubmitSignoff_RejectionImmediatelyRejects() {
        FinancialApprovalRequest req = FinancialApprovalRequest.builder()
                .id(1L)
                .communityId(1L)
                .status(ApprovalStatus.PENDING)
                .requiredSignoffs(2)
                .currentSignoffs(0)
                .build();

        when(requestRepository.findByIdAndCommunityId(1L, 1L)).thenReturn(Optional.of(req));
        when(signoffRepository.findByRequestIdAndApproverId(1L, 33L)).thenReturn(Optional.empty());
        when(requestRepository.save(any(FinancialApprovalRequest.class))).thenAnswer(i -> i.getArgument(0));

        SubmitSignoffRequestDto signoffDto = SubmitSignoffRequestDto.builder()
                .approverId(33L)
                .approverName("President Jane")
                .approverRole("PRESIDENT")
                .decision(SignoffDecision.REJECTED)
                .comments("Exceeds allocated quarterly budget")
                .build();

        ApprovalRequestDetailDto result = approvalService.submitSignoff(1L, 1L, signoffDto);

        assertNotNull(result);
        assertEquals(ApprovalStatus.REJECTED, result.getStatus());
    }
}
