package com.manacommunity.api.finance.service;

import com.manacommunity.api.billing.enums.InvoiceStatus;
import com.manacommunity.api.billing.model.CommunityInvoice;
import com.manacommunity.api.billing.model.CommunityPaymentTransaction;
import com.manacommunity.api.billing.model.ResidentAdvancePayment;
import com.manacommunity.api.billing.repository.CommunityInvoiceRepository;
import com.manacommunity.api.billing.repository.CommunityPaymentTransactionRepository;
import com.manacommunity.api.billing.repository.ResidentAdvancePaymentRepository;
import com.manacommunity.api.finance.dto.AgingReportDto;
import com.manacommunity.api.finance.dto.BalanceSheetDto;
import com.manacommunity.api.finance.dto.CashFlowSummaryDto;
import com.manacommunity.api.finance.dto.IncomeExpenseStatementDto;
import com.manacommunity.api.finance.repository.CommunityRefundRepository;
import com.manacommunity.api.repository.ExpenseRepository;
import com.manacommunity.api.repository.VendorInvoiceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountingReportServiceTest {

    @Mock
    private CommunityInvoiceRepository invoiceRepository;

    @Mock
    private CommunityPaymentTransactionRepository paymentRepository;

    @Mock
    private ResidentAdvancePaymentRepository advanceRepository;

    @Mock
    private CommunityRefundRepository refundRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private VendorInvoiceRepository vendorInvoiceRepository;

    @InjectMocks
    private AccountingReportService reportService;

    @Test
    void testGenerateIncomeExpenseStatement() {
        Long communityId = 1L;
        LocalDate start = LocalDate.now().minusMonths(1);
        LocalDate end = LocalDate.now();

        CommunityPaymentTransaction p1 = CommunityPaymentTransaction.builder().amount(BigDecimal.valueOf(10000.00)).build();
        when(paymentRepository.findByCommunityId(communityId)).thenReturn(List.of(p1));

        CommunityInvoice i1 = CommunityInvoice.builder()
                .status(InvoiceStatus.PAID)
                .penaltyAmount(BigDecimal.valueOf(250.00))
                .build();
        when(invoiceRepository.findByCommunityId(communityId)).thenReturn(List.of(i1));
        when(advanceRepository.findByCommunityId(communityId)).thenReturn(List.of());
        when(refundRepository.findByCommunityId(communityId, null)).thenReturn(new PageImpl<>(List.of()));
        when(vendorInvoiceRepository.findAll()).thenReturn(List.of());
        when(expenseRepository.sumAmountByCommunity(communityId)).thenReturn(BigDecimal.valueOf(4000.00));

        IncomeExpenseStatementDto statement = reportService.generateIncomeExpenseStatement(communityId, start, end);

        assertNotNull(statement);
        assertEquals(BigDecimal.valueOf(10250.00), statement.getTotalRevenue());
        assertEquals(BigDecimal.valueOf(4000.00), statement.getTotalExpenses());
        assertEquals(BigDecimal.valueOf(6250.00), statement.getNetSurplusOrDeficit());
    }

    @Test
    void testGenerateAgingReport_BucketsCorrectly() {
        Long communityId = 1L;
        LocalDate asOfDate = LocalDate.now();

        // Overdue 10 days (bucket 0-30)
        CommunityInvoice inv1 = CommunityInvoice.builder()
                .flatNumber("101")
                .tower("A")
                .status(InvoiceStatus.ISSUED)
                .dueDate(asOfDate.minusDays(10))
                .outstandingAmount(BigDecimal.valueOf(1500.00))
                .build();

        // Overdue 45 days (bucket 31-60)
        CommunityInvoice inv2 = CommunityInvoice.builder()
                .flatNumber("202")
                .tower("B")
                .status(InvoiceStatus.ISSUED)
                .dueDate(asOfDate.minusDays(45))
                .outstandingAmount(BigDecimal.valueOf(2500.00))
                .build();

        // Overdue 100 days (bucket >90)
        CommunityInvoice inv3 = CommunityInvoice.builder()
                .flatNumber("303")
                .tower("C")
                .status(InvoiceStatus.OVERDUE)
                .dueDate(asOfDate.minusDays(100))
                .outstandingAmount(BigDecimal.valueOf(5000.00))
                .build();

        when(invoiceRepository.findByCommunityId(communityId)).thenReturn(List.of(inv1, inv2, inv3));

        AgingReportDto aging = reportService.generateAgingReport(communityId, asOfDate);

        assertNotNull(aging);
        assertEquals(BigDecimal.valueOf(9000.00), aging.getTotalOverdue());
        assertEquals(BigDecimal.valueOf(1500.00), aging.getCurrentDue());
        assertEquals(BigDecimal.valueOf(2500.00), aging.getOverdue31To60Days());
        assertEquals(BigDecimal.valueOf(5000.00), aging.getOverdueOver90Days());
        assertEquals(3, aging.getFlatBreakdowns().size());
    }

    @Test
    void testGenerateBalanceSheet() {
        Long communityId = 1L;
        LocalDate asOf = LocalDate.now();

        CommunityPaymentTransaction p1 = CommunityPaymentTransaction.builder().amount(BigDecimal.valueOf(20000.00)).build();
        when(paymentRepository.findByCommunityId(communityId)).thenReturn(List.of(p1));

        CommunityInvoice i1 = CommunityInvoice.builder()
                .status(InvoiceStatus.ISSUED)
                .outstandingAmount(BigDecimal.valueOf(5000.00))
                .build();
        when(invoiceRepository.findByCommunityId(communityId)).thenReturn(List.of(i1));
        when(vendorInvoiceRepository.sumPendingPaymentAmount()).thenReturn(BigDecimal.valueOf(2000.00));
        when(advanceRepository.findByCommunityId(communityId)).thenReturn(List.of());

        BalanceSheetDto balanceSheet = reportService.generateBalanceSheet(communityId, asOf);

        assertNotNull(balanceSheet);
        assertEquals(BigDecimal.valueOf(25000.00), balanceSheet.getTotalAssets());
        assertEquals(BigDecimal.valueOf(2000.00), balanceSheet.getTotalLiabilities());
    }
}
