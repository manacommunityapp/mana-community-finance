package com.manacommunity.api.finance.repository;

import com.manacommunity.api.finance.entity.FinanceSalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface FinanceSalesOrderRepository extends JpaRepository<FinanceSalesOrder, Long> {
    List<FinanceSalesOrder> findAllByOrderByDocDateDesc();
    List<FinanceSalesOrder> findByStatusOrderByDocDateDesc(String status);

    @Query("SELECT COALESCE(SUM(s.grandTotal), 0) FROM FinanceSalesOrder s WHERE s.docDate BETWEEN :from AND :to")
    BigDecimal sumSalesBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT COALESCE(SUM(s.grandTotal), 0) FROM FinanceSalesOrder s WHERE s.docDate BETWEEN :from AND :to AND s.status = :status")
    BigDecimal sumSalesByStatusBetween(@Param("from") LocalDate from, @Param("to") LocalDate to, @Param("status") String status);
}