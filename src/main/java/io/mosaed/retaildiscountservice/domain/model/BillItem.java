package io.mosaed.retaildiscountservice.domain.model;

/**
 *
 * @author MOSAED ALOTAIBI
 */

import java.util.Objects;

public final class BillItem {

    private final String name;
    private final ItemCategory category;
    private final Money unitPrice;
    private final int quantity;

    private BillItem(String name, ItemCategory category, Money unitPrice, int quantity) {
        this.name = name;
        this.category = category;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    public static BillItem of(String name, ItemCategory category, Money unitPrice, int quantity) {
        validateInputs(name, category, unitPrice, quantity);
        return new BillItem(name, category, unitPrice, quantity);
    }

    private static void validateInputs(String name, ItemCategory category, Money unitPrice, int quantity) {
        Objects.requireNonNull(name, "Item name cannot be null");
        Objects.requireNonNull(category, "Item category cannot be null");
        Objects.requireNonNull(unitPrice, "Unit price cannot be null");

        if (name.trim().isEmpty()) {
            throw new IllegalArgumentException("Item name cannot be empty");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
    }

    public Money getTotalPrice() {
        return unitPrice.multiply(java.math.BigDecimal.valueOf(quantity));
    }

    public boolean isEligibleForPercentageDiscount() {
        return category.isEligibleForPercentageDiscount();
    }

    public Money getAmountEligibleForPercentageDiscount() {
        if (isEligibleForPercentageDiscount()) {
            return getTotalPrice();
        }
        return Money.zero();
    }

    public String getName() {
        return name;
    }

    public ItemCategory getCategory() {
        return category;
    }

    public Money getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BillItem billItem = (BillItem) o;
        return quantity == billItem.quantity &&
                Objects.equals(name, billItem.name) &&
                category == billItem.category &&
                Objects.equals(unitPrice, billItem.unitPrice);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, category, unitPrice, quantity);
    }

    @Override
    public String toString() {
        return String.format("BillItem{name='%s', category=%s, unitPrice=%s, quantity=%d, total=%s}",
                name, category, unitPrice, quantity, getTotalPrice());
    }
}