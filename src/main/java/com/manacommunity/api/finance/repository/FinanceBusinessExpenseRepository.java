package com.manacommunity.api.finance.repository;

import com.manacommunity.api.finance.entity.FinanceBusinessExpense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface FinanceBusinessExpenseRepository extends JpaRepository<FinanceBusinessExpense, Long> {
    List<FinanceBusinessExpense> findAllByOrderByDocDateDesc();
    List<FinanceBusinessExpense> findByStatusOrderByDocDateDesc(String status);

    @Query("SELECT COALESCE(SUM(e.grandTotal), 0) FROM FinanceBusinessExpense e WHERE e.docDate BETWEEN :from AND :to")
    BigDecimal sumExpensesBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT COALESCE(SUM(e.grandTotal), 0) FROM FinanceBusinessExpense e WHERE e.docDate BETWEEN :from AND :to AND e.status = :status")
    BigDecimal sumExpensesByStatusBetween(@Param("from") LocalDate from, @Param("to") LocalDate to, @Param("status") String status);

    long countByStatus(String status);

    long countByStatusAndDueDateBefore(String status, LocalDate date);
}