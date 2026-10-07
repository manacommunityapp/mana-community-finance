package com.manacommunity.api.finance.service;

import com.manacommunity.api.billing.model.CommunityPaymentTransaction;
import com.manacommunity.api.billing.repository.CommunityPaymentTransactionRepository;
import com.manacommunity.api.finance.dto.RefundRequestDto;
import com.manacommunity.api.finance.dto.RefundResponseDto;
import com.manacommunity.api.finance.entity.CommunityRefund;
import com.manacommunity.api.finance.entity.CommunityRefund.RefundStatus;
import com.manacommunity.api.finance.repository.CommunityRefundRepository;
import org.junit.jupiter.api.BeforeEach;
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
class FinanceRefundServiceTest {

    @Mock
    private CommunityRefundRepository refundRepository;

    @Mock
    private CommunityPaymentTransactionRepository paymentRepository;

    @InjectMocks
    private FinanceRefundService refundService;

    private CommunityPaymentTransaction mockPayment;
    private CommunityRefund mockRefund;

    @BeforeEach
    void setUp() {
        mockPayment = CommunityPaymentTransaction.builder()
                .id(100L)
                .communityId(1L)
                .amount(BigDecimal.valueOf(5000.00))
                .build();

        mockRefund = CommunityRefund.builder()
                .id(1L)
                .communityId(1L)
                .paymentId(100L)
                .residentId(5L)
                .refundAmount(BigDecimal.valueOf(1000.00))
                .status(RefundStatus.REQUESTED)
                .build();
    }

    @Test
    void testRequestRefund_Success() {
        RefundRequestDto dto = RefundRequestDto.builder()
                .communityId(1L)
                .paymentId(100L)
                .residentId(5L)
                .refundAmount(BigDecimal.valueOf(1000.00))
                .refundReason("Overpayment")
                .build();

        when(paymentRepository.findById(100L)).thenReturn(Optional.of(mockPayment));
        when(refundRepository.findByPaymentId(100L)).thenReturn(List.of());
        when(refundRepository.save(any(CommunityRefund.class))).thenAnswer(i -> {
            CommunityRefund r = i.getArgument(0);
            r.setId(1L);
            return r;
        });

        RefundResponseDto response = refundService.requestRefund(dto);

        assertNotNull(response);
        assertEquals(BigDecimal.valueOf(1000.00), response.getRefundAmount());
        assertEquals(RefundStatus.REQUESTED, response.getStatus());
        verify(refundRepository).save(any(CommunityRefund.class));
    }

    @Test
    void testRequestRefund_ExceedsPayment_ThrowsException() {
        RefundRequestDto dto = RefundRequestDto.builder()
                .communityId(1L)
                .paymentId(100L)
                .residentId(5L)
                .refundAmount(BigDecimal.valueOf(6000.00))
                .build();

        when(paymentRepository.findById(100L)).thenReturn(Optional.of(mockPayment));
        when(refundRepository.findByPaymentId(100L)).thenReturn(List.of());

        assertThrows(IllegalStateException.class, () -> refundService.requestRefund(dto));
    }

    @Test
    void testApproveAndProcessRefund() {
        when(refundRepository.findByIdAndCommunityId(1L, 1L)).thenReturn(Optional.of(mockRefund));
        when(refundRepository.save(any(CommunityRefund.class))).thenAnswer(i -> i.getArgument(0));

        RefundResponseDto approved = refundService.approveRefund(1L, 1L, 99L);
        assertEquals(RefundStatus.APPROVED, approved.getStatus());

        mockRefund.setStatus(RefundStatus.APPROVED);
        RefundResponseDto processed = refundService.processRefund(1L, 1L, "TXN-REF-12345", "Bank transfer executed");
        assertEquals(RefundStatus.PROCESSED, processed.getStatus());
        assertEquals("TXN-REF-12345", processed.getTransactionRef());
    }
}
