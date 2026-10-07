package com.manacommunity.api.billing.service;

import com.manacommunity.api.billing.dto.CommunityInvoiceDto;
import com.manacommunity.api.billing.dto.InvoiceItemDto;
import com.manacommunity.api.billing.enums.InvoiceStatus;
import com.manacommunity.api.billing.model.CommunityInvoice;
import com.manacommunity.api.billing.model.CommunityInvoiceItem;
import com.manacommunity.api.billing.repository.CommunityInvoiceItemRepository;
import com.manacommunity.api.billing.repository.CommunityInvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service("communityInvoiceService")
@RequiredArgsConstructor
public class InvoiceService {

    private final CommunityInvoiceRepository invoiceRepository;
    private final CommunityInvoiceItemRepository itemRepository;

    public CommunityInvoiceDto getInvoiceByNumber(String invoiceNumber) {
        CommunityInvoice inv = invoiceRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + invoiceNumber));
        return mapInvoice(inv);
    }

    public Page<CommunityInvoiceDto> getInvoicesForPeriod(Long communityId, String billingPeriod, Pageable pageable) {
        return invoiceRepository.findByCommunityIdAndBillingPeriod(communityId, billingPeriod, pageable)
                .map(this::mapInvoice);
    }

    public Page<CommunityInvoiceDto> getInvoicesForFlat(Long communityId, String flatNumber, String tower, Pageable pageable) {
        return invoiceRepository.findByCommunityIdAndFlatNumberAndTower(communityId, flatNumber, tower, pageable)
                .map(this::mapInvoice);
    }

    @Transactional
    public CommunityInvoiceDto applyAdjustment(Long invoiceId, BigDecimal adjustmentAmount, String reason) {
        CommunityInvoice inv = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + invoiceId));

        inv.setAdjustmentAmount(adjustmentAmount);
        inv.setTotalAmount(inv.getSubtotal().add(inv.getPenaltyAmount()).subtract(inv.getDiscountAmount()).add(adjustmentAmount));
        inv.setOutstandingAmount(inv.getTotalAmount().subtract(inv.getPaidAmount()));

        if (inv.getOutstandingAmount().compareTo(BigDecimal.ZERO) <= 0) {
            inv.setStatus(InvoiceStatus.PAID);
        }

        return mapInvoice(invoiceRepository.save(inv));
    }

    public CommunityInvoiceDto mapInvoice(CommunityInvoice i) {
        List<InvoiceItemDto> items = itemRepository.findByInvoiceId(i.getId())
                .stream().map(it -> InvoiceItemDto.builder()
                        .id(it.getId())
                        .invoiceId(it.getInvoiceId())
                        .componentType(it.getComponentType())
                        .description(it.getDescription())
                        .quantity(it.getQuantity())
                        .rate(it.getRate())
                        .amount(it.getAmount())
                        .build()).collect(Collectors.toList());

        return CommunityInvoiceDto.builder()
                .id(i.getId())
                .communityId(i.getCommunityId())
                .invoiceNumber(i.getInvoiceNumber())
                .flatId(i.getFlatId())
                .flatNumber(i.getFlatNumber())
                .tower(i.getTower())
                .billingPeriod(i.getBillingPeriod())
                .invoiceDate(i.getInvoiceDate())
                .dueDate(i.getDueDate())
                .subtotal(i.getSubtotal())
                .penaltyAmount(i.getPenaltyAmount())
                .discountAmount(i.getDiscountAmount())
                .adjustmentAmount(i.getAdjustmentAmount())
                .totalAmount(i.getTotalAmount())
                .paidAmount(i.getPaidAmount())
                .outstandingAmount(i.getOutstandingAmount())
                .status(i.getStatus())
                .responsiblePartyId(i.getResponsiblePartyId())
                .responsiblePartyType(i.getResponsiblePartyType())
                .items(items)
                .createdAt(i.getCreatedAt())
                .updatedAt(i.getUpdatedAt())
                .build();
    }
}
