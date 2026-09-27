package com.manacommunity.api.billing.repository;

import com.manacommunity.api.billing.model.CommunityInvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommunityInvoiceItemRepository extends JpaRepository<CommunityInvoiceItem, Long> {
    List<CommunityInvoiceItem> findByInvoiceId(Long invoiceId);
}
