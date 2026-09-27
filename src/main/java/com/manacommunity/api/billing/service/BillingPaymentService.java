package com.manacommunity.api.billing.service;

import com.manacommunity.api.billing.dto.*;
import com.manacommunity.api.billing.enums.InvoiceStatus;
import com.manacommunity.api.billing.enums.PaymentMode;
import com.manacommunity.api.billing.model.CommunityInvoice;
import com.manacommunity.api.billing.model.CommunityPaymentTransaction;
import com.manacommunity.api.billing.model.CommunityReceipt;
import com.manacommunity.api.billing.model.ResidentAdvancePayment;
import com.manacommunity.api.billing.repository.CommunityInvoiceRepository;
import com.manacommunity.api.billing.repository.CommunityPaymentTransactionRepository;
import com.manacommunity.api.billing.repository.CommunityReceiptRepository;
import com.manacommunity.api.billing.repository.ResidentAdvancePaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillingPaymentService {

    private final CommunityInvoiceRepository invoiceRepository;
    private final CommunityPaymentTransactionRepository transactionRepository;
    private final CommunityReceiptRepository receiptRepository;
    private final ResidentAdvancePaymentRepository advancePaymentRepository;

    @Transactional
    public PaymentTransactionResponse processPayment(Long communityId, ProcessPaymentRequest req, Long userId, String userName) {
        CommunityInvoice invoice = invoiceRepository.findById(req.getInvoiceId())
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + req.getInvoiceId()));

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new IllegalStateException("Invoice is already fully paid.");
        }

        if (req.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero.");
        }

        // Cap payment at outstanding amount
        BigDecimal payableAmount = req.getAmount().min(invoice.getOutstandingAmount());
        BigDecimal excessAmount = req.getAmount().subtract(payableAmount);

        // 1. Create Transaction
        String txRef = "PAY-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "-"
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        CommunityPaymentTransaction tx = CommunityPaymentTransaction.builder()
                .communityId(communityId)
                .transactionRef(txRef)
                .invoiceId(invoice.getId())
                .flatId(invoice.getFlatId())
                .flatNumber(invoice.getFlatNumber())
                .tower(invoice.getTower())
                .amount(payableAmount)
                .paymentMode(req.getPaymentMode())
                .payerUserId(userId)
                .payerName(userName)
                .status("SUCCESS")
                .gatewayRef(req.getGatewayRef())
                .build();

        CommunityPaymentTransaction savedTx = transactionRepository.save(tx);

        // 2. Update Invoice balance
        invoice.setPaidAmount(invoice.getPaidAmount().add(payableAmount));
        invoice.setOutstandingAmount(invoice.getTotalAmount().subtract(invoice.getPaidAmount()));

        if (invoice.getOutstandingAmount().compareTo(BigDecimal.ZERO) == 0) {
            invoice.setStatus(InvoiceStatus.PAID);
        } else {
            invoice.setStatus(InvoiceStatus.PARTIALLY_PAID);
        }
        invoiceRepository.save(invoice);

        // 3. Issue immutable Receipt
        String receiptNumber = "REC-" + LocalDate.now().getYear() + "-"
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        CommunityReceipt receipt = CommunityReceipt.builder()
                .communityId(communityId)
                .receiptNumber(receiptNumber)
                .invoiceId(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .transactionId(savedTx.getId())
                .amountPaid(payableAmount)
                .issuedToName(userName)
                .flatNumber(invoice.getFlatNumber())
                .tower(invoice.getTower())
                .build();
        receiptRepository.save(receipt);

        // 4. Handle excess amount as Advance Deposit
        if (excessAmount.compareTo(BigDecimal.ZERO) > 0) {
            depositAdvance(communityId, AdvanceDepositRequest.builder()
                    .flatNumber(invoice.getFlatNumber())
                    .tower(invoice.getTower())
                    .amount(excessAmount)
                    .paymentMode(req.getPaymentMode())
                    .transactionRef(txRef)
                    .build(), userId);
        }

        log.info("Payment of ₹{} processed for invoice {}. Receipt: {}", payableAmount, invoice.getInvoiceNumber(), receiptNumber);

        return PaymentTransactionResponse.builder()
                .id(savedTx.getId())
                .communityId(savedTx.getCommunityId())
                .transactionRef(savedTx.getTransactionRef())
                .invoiceId(savedTx.getInvoiceId())
                .flatId(savedTx.getFlatId())
                .flatNumber(savedTx.getFlatNumber())
                .tower(savedTx.getTower())
                .amount(savedTx.getAmount())
                .paymentMode(savedTx.getPaymentMode())
                .payerUserId(savedTx.getPayerUserId())
                .payerName(savedTx.getPayerName())
                .status(savedTx.getStatus())
                .gatewayRef(savedTx.getGatewayRef())
                .paymentDate(savedTx.getPaymentDate())
                .receiptNumber(receiptNumber)
                .build();
    }

    // Advance Payment Management
    @Transactional
    public AdvanceBalanceResponse depositAdvance(Long communityId, AdvanceDepositRequest req, Long userId) {
        ResidentAdvancePayment adv = advancePaymentRepository
                .findByCommunityIdAndFlatNumberAndTower(communityId, req.getFlatNumber(), req.getTower())
                .orElse(ResidentAdvancePayment.builder()
                        .communityId(communityId)
                        .flatNumber(req.getFlatNumber())
                        .tower(req.getTower())
                        .userId(userId)
                        .balanceAmount(BigDecimal.ZERO)
                        .totalDeposited(BigDecimal.ZERO)
                        .totalUtilized(BigDecimal.ZERO)
                        .build());

        adv.setBalanceAmount(adv.getBalanceAmount().add(req.getAmount()));
        adv.setTotalDeposited(adv.getTotalDeposited().add(req.getAmount()));

        ResidentAdvancePayment saved = advancePaymentRepository.save(adv);
        return mapAdvance(saved);
    }

    public AdvanceBalanceResponse getAdvanceBalance(Long communityId, String flatNumber, String tower) {
        return advancePaymentRepository.findByCommunityIdAndFlatNumberAndTower(communityId, flatNumber, tower)
                .map(this::mapAdvance)
                .orElse(AdvanceBalanceResponse.builder()
                        .communityId(communityId)
                        .flatNumber(flatNumber)
                        .tower(tower)
                        .balanceAmount(BigDecimal.ZERO)
                        .totalDeposited(BigDecimal.ZERO)
                        .totalUtilized(BigDecimal.ZERO)
                        .build());
    }

    public List<ReceiptDto> getReceiptsForInvoice(Long invoiceId) {
        return receiptRepository.findByInvoiceId(invoiceId)
                .stream().map(this::mapReceipt).collect(Collectors.toList());
    }

    public List<ReceiptDto> getReceiptsForFlat(Long communityId, String flatNumber, String tower) {
        return receiptRepository.findByCommunityIdAndFlatNumberAndTower(communityId, flatNumber, tower)
                .stream().map(this::mapReceipt).collect(Collectors.toList());
    }

    public ReceiptDto getReceiptByNumber(String receiptNumber) {
        CommunityReceipt r = receiptRepository.findByReceiptNumber(receiptNumber)
                .orElseThrow(() -> new IllegalArgumentException("Receipt not found: " + receiptNumber));
        return mapReceipt(r);
    }

    private AdvanceBalanceResponse mapAdvance(ResidentAdvancePayment a) {
        return AdvanceBalanceResponse.builder()
                .id(a.getId())
                .communityId(a.getCommunityId())
                .flatNumber(a.getFlatNumber())
                .tower(a.getTower())
                .balanceAmount(a.getBalanceAmount())
                .totalDeposited(a.getTotalDeposited())
                .totalUtilized(a.getTotalUtilized())
                .lastUtilizedAt(a.getLastUtilizedAt())
                .build();
    }

    private ReceiptDto mapReceipt(CommunityReceipt r) {
        return ReceiptDto.builder()
                .id(r.getId())
                .communityId(r.getCommunityId())
                .receiptNumber(r.getReceiptNumber())
                .invoiceId(r.getInvoiceId())
                .invoiceNumber(r.getInvoiceNumber())
                .transactionId(r.getTransactionId())
                .amountPaid(r.getAmountPaid())
                .issuedToName(r.getIssuedToName())
                .flatNumber(r.getFlatNumber())
                .tower(r.getTower())
                .receiptPdfUrl(r.getReceiptPdfUrl())
                .receiptDate(r.getReceiptDate())
                .build();
    }
}
