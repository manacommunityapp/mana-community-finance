package com.manacommunity.api.finance.repository;

import com.manacommunity.api.finance.entity.VendorPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface VendorPaymentRepository extends JpaRepository<VendorPayment, Long> {
    List<VendorPayment> findAllByOrderByPaymentDateDesc();
    List<VendorPayment> findByPaymentTypeOrderByPaymentDateDesc(VendorPayment.PaymentType paymentType);

    @Query("SELECT COALESCE(SUM(v.amount), 0) FROM VendorPayment v WHERE v.paymentDate BETWEEN :from AND :to")
    BigDecimal sumPaymentsBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT v.vendorName, COALESCE(SUM(v.amount), 0) FROM VendorPayment v WHERE v.paymentDate BETWEEN :from AND :to GROUP BY v.vendorName ORDER BY 2 DESC")
    List<Object[]> topVendorsBySpend(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT COALESCE(SUM(v.amount), 0) FROM VendorPayment v WHERE v.paymentDate BETWEEN :from AND :to AND v.status = :status")
    BigDecimal sumPaymentsByStatusBetween(@Param("from") LocalDate from, @Param("to") LocalDate to, @Param("status") String status);

    @Query("SELECT FUNCTION('DATE_FORMAT', v.paymentDate, '%Y-%m'), COALESCE(SUM(v.amount), 0) FROM VendorPayment v WHERE v.paymentDate BETWEEN :from AND :to GROUP BY FUNCTION('DATE_FORMAT', v.paymentDate, '%Y-%m') ORDER BY 1")
    List<Object[]> monthlySpend(@Param("from") LocalDate from, @Param("to") LocalDate to);
}