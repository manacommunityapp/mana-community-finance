package com.manacommunity.api.finance.service;

import com.manacommunity.api.billing.model.CommunityPaymentTransaction;
import com.manacommunity.api.billing.repository.CommunityPaymentTransactionRepository;
import com.manacommunity.api.finance.dto.ReconciliationResultDto;
import com.manacommunity.api.finance.dto.ReconciliationStatementItemDto;
import com.manacommunity.api.finance.dto.SettlementRecordRequestDto;
import com.manacommunity.api.finance.entity.GatewaySettlementRecord;
import com.manacommunity.api.finance.entity.ReconciliationBatch;
import com.manacommunity.api.finance.entity.ReconciliationRecord;
import com.manacommunity.api.finance.repository.GatewaySettlementRecordRepository;
import com.manacommunity.api.finance.repository.ReconciliationBatchRepository;
import com.manacommunity.api.finance.repository.ReconciliationRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinanceReconciliationServiceTest {

    @Mock
    private ReconciliationBatchRepository batchRepository;

    @Mock
    private ReconciliationRecordRepository recordRepository;

    @Mock
    private GatewaySettlementRecordRepository settlementRepository;

    @Mock
    private CommunityPaymentTransactionRepository paymentRepository;

    @InjectMocks
    private FinanceReconciliationService reconciliationService;

    @Test
    void testReconcileStatement_MatchedAndMismatched() {
        Long communityId = 1L;
        String gateway = "RAZORPAY";
        LocalDate date = LocalDate.now();

        List<ReconciliationStatementItemDto> items = List.of(
                ReconciliationStatementItemDto.builder()
                        .transactionRef("TXN-001")
                        .amount(BigDecimal.valueOf(2500.00))
                        .build(),
                ReconciliationStatementItemDto.builder()
                        .transactionRef("TXN-002")
                        .amount(BigDecimal.valueOf(3000.00))
                        .build()
        );

        when(batchRepository.save(any(ReconciliationBatch.class))).thenAnswer(i -> {
            ReconciliationBatch b = i.getArgument(0);
            b.setId(10L);
            return b;
        });

        // TXN-001 matches internal transaction
        CommunityPaymentTransaction txn1 = CommunityPaymentTransaction.builder()
                .transactionRef("TXN-001")
                .amount(BigDecimal.valueOf(2500.00))
                .build();
        when(paymentRepository.findByTransactionRef("TXN-001")).thenReturn(Optional.of(txn1));

        // TXN-002 has amount mismatch (internal is 2800)
        CommunityPaymentTransaction txn2 = CommunityPaymentTransaction.builder()
                .transactionRef("TXN-002")
                .amount(BigDecimal.valueOf(2800.00))
                .build();
        when(paymentRepository.findByTransactionRef("TXN-002")).thenReturn(Optional.of(txn2));

        when(recordRepository.save(any(ReconciliationRecord.class))).thenAnswer(i -> i.getArgument(0));

        ReconciliationResultDto result = reconciliationService.reconcileStatement(communityId, gateway, date, items);

        assertNotNull(result);
        assertEquals(2, result.getTotalTransactions());
        assertEquals(1, result.getMatchedCount());
        assertEquals(1, result.getMismatchedCount());
        assertEquals(ReconciliationBatch.BatchStatus.HAS_DISCREPANCIES, result.getStatus());
    }

    @Test
    void testRecordSettlement_CalculatesNetPayout() {
        SettlementRecordRequestDto dto = SettlementRecordRequestDto.builder()
                .communityId(1L)
                .gatewayName("RAZORPAY")
                .settlementDate(LocalDate.now())
                .grossAmount(BigDecimal.valueOf(100000.00))
                .gatewayFee(BigDecimal.valueOf(2000.00))
                .gstOnFee(BigDecimal.valueOf(360.00))
                .utrNumber("UTR123456789")
                .bankAccountRef("HDFC-9876")
                .build();

        when(settlementRepository.save(any(GatewaySettlementRecord.class))).thenAnswer(i -> {
            GatewaySettlementRecord rec = i.getArgument(0);
            rec.setId(50L);
            return rec;
        });

        GatewaySettlementRecord saved = reconciliationService.recordSettlement(dto);

        assertNotNull(saved);
        assertEquals(BigDecimal.valueOf(97640.00), saved.getNetPayoutAmount());
        assertEquals(GatewaySettlementRecord.PayoutStatus.SETTLED, saved.getPayoutStatus());
        verify(settlementRepository).save(any(GatewaySettlementRecord.class));
    }
}
