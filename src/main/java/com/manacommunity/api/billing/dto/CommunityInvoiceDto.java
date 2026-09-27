package com.manacommunity.api.billing.dto;

import com.manacommunity.api.billing.enums.InvoiceStatus;
import com.manacommunity.api.billing.enums.ResponsiblePartyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommunityInvoiceDto {
    private Long id;
    private Long communityId;
    private String invoiceNumber;
    private Long flatId;
    private String flatNumber;
    private String tower;
    private String billingPeriod;
    private LocalDate invoiceDate;
    private LocalDate dueDate;
    private BigDecimal subtotal;
    private BigDecimal penaltyAmount;
    private BigDecimal discountAmount;
    private BigDecimal adjustmentAmount;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal outstandingAmount;
    private InvoiceStatus status;
    private Long responsiblePartyId;
    private ResponsiblePartyType responsiblePartyType;
    private List<InvoiceItemDto> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
