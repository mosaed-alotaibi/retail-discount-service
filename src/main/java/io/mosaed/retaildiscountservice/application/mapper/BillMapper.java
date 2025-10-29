package io.mosaed.retaildiscountservice.application.mapper;

/**
 *
 * @author MOSAED ALOTAIBI
 */

import io.mosaed.retaildiscountservice.application.dto.BillCalculationResponse;
import io.mosaed.retaildiscountservice.application.dto.BillItemDto;
import io.mosaed.retaildiscountservice.domain.exception.InvalidBillException;
import io.mosaed.retaildiscountservice.domain.model.Bill;
import io.mosaed.retaildiscountservice.domain.model.BillItem;
import io.mosaed.retaildiscountservice.domain.model.ItemCategory;
import io.mosaed.retaildiscountservice.domain.model.Money;

import java.util.List;
import java.util.stream.Collectors;

public final class BillMapper {

    // Private constructor prevents instantiation
    private BillMapper() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static BillItem toDomainItem(BillItemDto dto) {
        try {
            // Convert string category to enum with validation
            ItemCategory category = parseCategory(dto.getCategory());

            // Convert BigDecimal to Money value object
            Money unitPrice = Money.of(dto.getUnitPrice());

            // Create the domain object using its factory method
            // The domain object will perform its own validation
            return BillItem.of(
                    dto.getName(),
                    category,
                    unitPrice,
                    dto.getQuantity()
            );
        } catch (InvalidBillException e) {
            // Re-wrap InvalidBillException from parseCategory with item name
            throw new InvalidBillException(
                    String.format("Invalid bill item '%s': %s", dto.getName(), e.getMessage()),
                    e
            );
        } catch (IllegalArgumentException e) {
            // Wrap domain validation errors in our application exception
            throw new InvalidBillException(
                    String.format("Invalid bill item '%s': %s", dto.getName(), e.getMessage()),
                    e
            );
        }
    }

    public static List<BillItem> toDomainItems(List<BillItemDto> dtos) {
        return dtos.stream()
                .map(BillMapper::toDomainItem)
                .collect(Collectors.toList());
    }

    public static BillCalculationResponse toResponse(Bill bill) {
        // Get the discount breakdown from the Bill
        Bill.DiscountBreakdown breakdown = bill.calculateDiscount();

        // Build the response DTO using the builder pattern
        return BillCalculationResponse.builder()
                .billId(bill.getBillId())
                .customerId(bill.getCustomer().getCustomerId())
                .calculatedAt(bill.getCreatedAt())
                .totalAmount(breakdown.getTotalAmount().getAmount())
                .percentageDiscount(breakdown.getPercentageDiscount().getAmount())
                .percentageDiscountRate(breakdown.getPercentageDiscountRate())
                .billBasedDiscount(breakdown.getBillBasedDiscount().getAmount())
                .totalDiscount(breakdown.getTotalDiscount().getAmount())
                .netPayable(breakdown.getNetPayable().getAmount())
                .build();
    }

    private static ItemCategory parseCategory(String categoryString) {
        if (categoryString == null || categoryString.trim().isEmpty()) {
            throw new InvalidBillException("Item category cannot be empty");
        }

        try {
            // Convert to uppercase and trim for lenient parsing
            return ItemCategory.valueOf(categoryString.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            // Build a helpful error message showing valid options
            String validCategories = String.join(", ", getValidCategories());
            throw new InvalidBillException(
                    String.format("Invalid category '%s'. Valid categories are: %s",
                            categoryString, validCategories)
            );
        }
    }

    private static List<String> getValidCategories() {
        return List.of(ItemCategory.values()).stream()
                .map(Enum::name)
                .collect(Collectors.toList());
    }
}
