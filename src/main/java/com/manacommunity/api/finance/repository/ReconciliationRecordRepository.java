package com.manacommunity.api.finance.repository;

import com.manacommunity.api.finance.entity.ReconciliationRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReconciliationRecordRepository extends JpaRepository<ReconciliationRecord, Long> {
    List<ReconciliationRecord> findByBatchId(Long batchId);
    List<ReconciliationRecord> findByBatchIdAndStatus(Long batchId, ReconciliationRecord.ReconciliationStatus status);
}
