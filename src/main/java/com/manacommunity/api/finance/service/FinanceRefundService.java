package com.manacommunity.api.finance.service;

import com.manacommunity.api.billing.model.CommunityPaymentTransaction;
import com.manacommunity.api.billing.repository.CommunityPaymentTransactionRepository;
import com.manacommunity.api.finance.dto.RefundRequestDto;
import com.manacommunity.api.finance.dto.RefundResponseDto;
import com.manacommunity.api.finance.entity.CommunityRefund;
import com.manacommunity.api.finance.entity.CommunityRefund.RefundStatus;
import com.manacommunity.api.finance.repository.CommunityRefundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FinanceRefundService {

    private final CommunityRefundRepository refundRepository;
    private final CommunityPaymentTransactionRepository paymentRepository;

    @Transactional
    public RefundResponseDto requestRefund(RefundRequestDto dto) {
        if (dto.getRefundAmount() == null || dto.getRefundAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Refund amount must be greater than zero");
        }

        if (dto.getPaymentId() != null) {
            CommunityPaymentTransaction payment = paymentRepository.findById(dto.getPaymentId())
                    .orElseThrow(() -> new IllegalArgumentException("Payment transaction not found: " + dto.getPaymentId()));

            List<CommunityRefund> priorRefunds = refundRepository.findByPaymentId(dto.getPaymentId());
            BigDecimal alreadyRefunded = priorRefunds.stream()
                    .filter(r -> r.getStatus() != RefundStatus.REJECTED)
                    .map(CommunityRefund::getRefundAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal refundableRemaining = payment.getAmount().subtract(alreadyRefunded);
            if (dto.getRefundAmount().compareTo(refundableRemaining) > 0) {
                throw new IllegalStateException("Requested refund exceeds remaining payment balance. Maximum refundable: " + refundableRemaining);
            }
        }

        CommunityRefund refund = CommunityRefund.builder()
                .communityId(dto.getCommunityId())
                .paymentId(dto.getPaymentId())
                .invoiceId(dto.getInvoiceId())
                .residentId(dto.getResidentId())
                .refundAmount(dto.getRefundAmount())
                .refundReason(dto.getRefundReason())
                .refundMode(dto.getRefundMode() != null ? dto.getRefundMode() : "ORIGINAL_PAYMENT_METHOD")
                .status(RefundStatus.REQUESTED)
                .createdAt(LocalDateTime.now())
                .build();

        return mapToDto(refundRepository.save(refund));
    }

    @Transactional
    public RefundResponseDto approveRefund(Long id, Long communityId, Long approvedBy) {
        CommunityRefund refund = refundRepository.findByIdAndCommunityId(id, communityId)
                .orElseThrow(() -> new IllegalArgumentException("Refund record not found: " + id));

        if (refund.getStatus() != RefundStatus.REQUESTED && refund.getStatus() != RefundStatus.UNDER_REVIEW) {
            throw new IllegalStateException("Only REQUESTED or UNDER_REVIEW refunds can be approved");
        }

        refund.setStatus(RefundStatus.APPROVED);
        refund.setApprovedBy(approvedBy);
        return mapToDto(refundRepository.save(refund));
    }

    @Transactional
    public RefundResponseDto processRefund(Long id, Long communityId, String transactionRef, String remarks) {
        CommunityRefund refund = refundRepository.findByIdAndCommunityId(id, communityId)
                .orElseThrow(() -> new IllegalArgumentException("Refund record not found: " + id));

        if (refund.getStatus() != RefundStatus.APPROVED) {
            throw new IllegalStateException("Only APPROVED refunds can be processed");
        }

        refund.setStatus(RefundStatus.PROCESSED);
        refund.setTransactionRef(transactionRef);
        refund.setRemarks(remarks);
        refund.setProcessedAt(LocalDateTime.now());
        return mapToDto(refundRepository.save(refund));
    }

    @Transactional
    public RefundResponseDto rejectRefund(Long id, Long communityId, String remarks) {
        CommunityRefund refund = refundRepository.findByIdAndCommunityId(id, communityId)
                .orElseThrow(() -> new IllegalArgumentException("Refund record not found: " + id));

        if (refund.getStatus() == RefundStatus.PROCESSED) {
            throw new IllegalStateException("Cannot reject an already PROCESSED refund");
        }

        refund.setStatus(RefundStatus.REJECTED);
        refund.setRemarks(remarks);
        return mapToDto(refundRepository.save(refund));
    }

    @Transactional(readOnly = true)
    public Page<RefundResponseDto> getRefundsByCommunity(Long communityId, Pageable pageable) {
        return refundRepository.findByCommunityId(communityId, pageable).map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public RefundResponseDto getRefundById(Long id, Long communityId) {
        CommunityRefund refund = refundRepository.findByIdAndCommunityId(id, communityId)
                .orElseThrow(() -> new IllegalArgumentException("Refund record not found: " + id));
        return mapToDto(refund);
    }

    private RefundResponseDto mapToDto(CommunityRefund r) {
        return RefundResponseDto.builder()
                .id(r.getId())
                .communityId(r.getCommunityId())
                .paymentId(r.getPaymentId())
                .invoiceId(r.getInvoiceId())
                .residentId(r.getResidentId())
                .refundAmount(r.getRefundAmount())
                .refundReason(r.getRefundReason())
                .refundMode(r.getRefundMode())
                .status(r.getStatus())
                .transactionRef(r.getTransactionRef())
                .remarks(r.getRemarks())
                .approvedBy(r.getApprovedBy())
                .createdAt(r.getCreatedAt())
                .processedAt(r.getProcessedAt())
                .build();
    }
}
