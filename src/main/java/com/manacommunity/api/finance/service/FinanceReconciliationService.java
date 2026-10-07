package com.manacommunity.api.finance.service;

import com.manacommunity.api.billing.model.CommunityPaymentTransaction;
import com.manacommunity.api.billing.repository.CommunityPaymentTransactionRepository;
import com.manacommunity.api.finance.dto.ReconciliationResultDto;
import com.manacommunity.api.finance.dto.ReconciliationStatementItemDto;
import com.manacommunity.api.finance.dto.SettlementRecordRequestDto;
import com.manacommunity.api.finance.entity.GatewaySettlementRecord;
import com.manacommunity.api.finance.entity.ReconciliationBatch;
import com.manacommunity.api.finance.entity.ReconciliationBatch.BatchStatus;
import com.manacommunity.api.finance.entity.ReconciliationRecord;
import com.manacommunity.api.finance.entity.ReconciliationRecord.ReconciliationStatus;
import com.manacommunity.api.finance.repository.GatewaySettlementRecordRepository;
import com.manacommunity.api.finance.repository.ReconciliationBatchRepository;
import com.manacommunity.api.finance.repository.ReconciliationRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FinanceReconciliationService {

    private final ReconciliationBatchRepository batchRepository;
    private final ReconciliationRecordRepository recordRepository;
    private final GatewaySettlementRecordRepository settlementRepository;
    private final CommunityPaymentTransactionRepository paymentRepository;

    @Transactional
    public ReconciliationResultDto reconcileStatement(
            Long communityId,
            String gatewayName,
            LocalDate statementDate,
            List<ReconciliationStatementItemDto> statementItems) {

        ReconciliationBatch batch = ReconciliationBatch.builder()
                .communityId(communityId)
                .gatewayName(gatewayName)
                .statementDate(statementDate)
                .totalTransactions(statementItems.size())
                .matchedCount(0)
                .mismatchedCount(0)
                .status(BatchStatus.IN_PROGRESS)
                .processedAt(LocalDateTime.now())
                .build();

        batch = batchRepository.save(batch);

        int matched = 0;
        int mismatched = 0;
        List<ReconciliationRecord> records = new ArrayList<>();

        for (ReconciliationStatementItemDto item : statementItems) {
            Optional<CommunityPaymentTransaction> internalOpt = paymentRepository.findByTransactionRef(item.getTransactionRef());

            ReconciliationRecord rec = ReconciliationRecord.builder()
                    .batchId(batch.getId())
                    .transactionRef(item.getTransactionRef())
                    .gatewayAmount(item.getAmount())
                    .build();

            if (internalOpt.isPresent()) {
                CommunityPaymentTransaction internal = internalOpt.get();
                rec.setInternalAmount(internal.getAmount());

                if (internal.getAmount().compareTo(item.getAmount()) == 0) {
                    rec.setStatus(ReconciliationStatus.MATCHED);
                    matched++;
                } else {
                    rec.setStatus(ReconciliationStatus.MISMATCHED);
                    rec.setDiscrepancyReason("Amount discrepancy: gateway=" + item.getAmount() + ", system=" + internal.getAmount());
                    mismatched++;
                }
            } else {
                rec.setStatus(ReconciliationStatus.MISSING_INTERNAL);
                rec.setDiscrepancyReason("Transaction not found in internal payment ledger");
                mismatched++;
            }

            records.add(recordRepository.save(rec));
        }

        batch.setMatchedCount(matched);
        batch.setMismatchedCount(mismatched);
        batch.setStatus(mismatched == 0 ? BatchStatus.RECONCILED : BatchStatus.HAS_DISCREPANCIES);
        batch = batchRepository.save(batch);

        return mapResult(batch, records);
    }

    @Transactional
    public GatewaySettlementRecord recordSettlement(SettlementRecordRequestDto dto) {
        BigDecimal fee = dto.getGatewayFee() != null ? dto.getGatewayFee() : BigDecimal.ZERO;
        BigDecimal gst = dto.getGstOnFee() != null ? dto.getGstOnFee() : BigDecimal.ZERO;
        BigDecimal net = dto.getGrossAmount().subtract(fee).subtract(gst);

        GatewaySettlementRecord record = GatewaySettlementRecord.builder()
                .communityId(dto.getCommunityId())
                .gatewayName(dto.getGatewayName())
                .settlementDate(dto.getSettlementDate())
                .grossAmount(dto.getGrossAmount())
                .gatewayFee(fee)
                .gstOnFee(gst)
                .netPayoutAmount(net)
                .payoutStatus(GatewaySettlementRecord.PayoutStatus.SETTLED)
                .utrNumber(dto.getUtrNumber())
                .bankAccountRef(dto.getBankAccountRef())
                .createdAt(LocalDateTime.now())
                .build();

        return settlementRepository.save(record);
    }

    @Transactional(readOnly = true)
    public Page<ReconciliationBatch> getBatches(Long communityId, Pageable pageable) {
        return batchRepository.findByCommunityIdOrderByProcessedAtDesc(communityId, pageable);
    }

    @Transactional(readOnly = true)
    public ReconciliationResultDto getBatchDetails(Long batchId, Long communityId) {
        ReconciliationBatch batch = batchRepository.findByIdAndCommunityId(batchId, communityId)
                .orElseThrow(() -> new IllegalArgumentException("Reconciliation batch not found: " + batchId));
        List<ReconciliationRecord> records = recordRepository.findByBatchId(batchId);
        return mapResult(batch, records);
    }

    @Transactional(readOnly = true)
    public Page<GatewaySettlementRecord> getSettlements(Long communityId, Pageable pageable) {
        return settlementRepository.findByCommunityIdOrderBySettlementDateDesc(communityId, pageable);
    }

    private ReconciliationResultDto mapResult(ReconciliationBatch batch, List<ReconciliationRecord> records) {
        return ReconciliationResultDto.builder()
                .batchId(batch.getId())
                .communityId(batch.getCommunityId())
                .gatewayName(batch.getGatewayName())
                .statementDate(batch.getStatementDate())
                .totalTransactions(batch.getTotalTransactions())
                .matchedCount(batch.getMatchedCount())
                .mismatchedCount(batch.getMismatchedCount())
                .status(batch.getStatus())
                .processedAt(batch.getProcessedAt())
                .records(records.stream().map(r -> ReconciliationResultDto.ReconciliationRecordDto.builder()
                        .transactionRef(r.getTransactionRef())
                        .gatewayAmount(r.getGatewayAmount())
                        .internalAmount(r.getInternalAmount())
                        .status(r.getStatus().name())
                        .discrepancyReason(r.getDiscrepancyReason())
                        .build()).collect(Collectors.toList()))
                .build();
    }
}
