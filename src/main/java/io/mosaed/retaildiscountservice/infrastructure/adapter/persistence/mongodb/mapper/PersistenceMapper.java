package io.mosaed.retaildiscountservice.infrastructure.adapter.persistence.mongodb.mapper;

/**
 *
 * @author MOSAED ALOTAIBI
 */

import io.mosaed.retaildiscountservice.domain.model.*;
import io.mosaed.retaildiscountservice.infrastructure.adapter.persistence.mongodb.document.*;

import java.util.List;
import java.util.stream.Collectors;

public final class PersistenceMapper {

    private PersistenceMapper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static CustomerDocument toDocument(Customer customer) {
        return new CustomerDocument(
                customer.getCustomerId(),
                customer.getExplicitType().name(), // Enum → String
                customer.getRegistrationDate(),
                null
        );
    }

    public static Customer toDomain(CustomerDocument doc) {
        CustomerType type = CustomerType.valueOf(doc.getCustomerType());
        return Customer.of(
                doc.getCustomerId(),
                type,
                doc.getRegistrationDate()
        );
    }

    public static BillItemDocument toDocument(BillItem item) {
        return new BillItemDocument(
                item.getName(),
                item.getCategory().name(), // Enum → String
                item.getUnitPrice().getAmount(), // Money → BigDecimal
                item.getQuantity()
        );
    }

    public static BillItem toDomain(BillItemDocument doc) {
        ItemCategory category = ItemCategory.valueOf(doc.getCategory());
        Money unitPrice = Money.of(doc.getUnitPrice());

        return BillItem.of(
                doc.getName(),
                category,
                unitPrice,
                doc.getQuantity()
        );
    }

    public static List<BillItemDocument> toDocuments(List<BillItem> items) {
        return items.stream()
                .map(PersistenceMapper::toDocument)
                .collect(Collectors.toList());
    }

    public static List<BillItem> toDomainItems(List<BillItemDocument> docs) {
        return docs.stream()
                .map(PersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }

    public static BillDocument toDocument(Bill bill) {
        // convert embedded items
        List<BillItemDocument> itemDocs = toDocuments(bill.getItems());

        Bill.DiscountBreakdown breakdown = bill.calculateDiscount();

        // build the document with all data
        return new BillDocument(
                bill.getBillId(),
                bill.getCustomer().getCustomerId(),
                bill.getCustomer().getEffectiveType().name(), // Store for display
                itemDocs,
                bill.getCreatedAt(),
                // Store all calculated values
                breakdown.getTotalAmount().getAmount(),
                breakdown.getPercentageDiscount().getAmount(),
                breakdown.getPercentageDiscountRate(),
                breakdown.getBillBasedDiscount().getAmount(),
                breakdown.getTotalDiscount().getAmount(),
                breakdown.getNetPayable().getAmount()
        );
    }

    public static Bill toDomain(BillDocument doc, Customer customer) {
        // convert document items back to domain items
        List<BillItem> items = toDomainItems(doc.getItems());

        // rebuild the bill with its original ID and timestamp
        return Bill.reconstitute(
                doc.getBillId(),
                customer,
                items,
                doc.getCreatedAt()
        );
    }
}
