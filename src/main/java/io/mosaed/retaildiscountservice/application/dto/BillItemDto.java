package io.mosaed.retaildiscountservice.application.dto;

/**
 *
 * @author MOSAED ALOTAIBI
 */

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.Objects;

public final class BillItemDto {

    @NotBlank(message = "Item name is required and cannot be blank")
    private final String name;

    @NotBlank(message = "Item category is required and cannot be blank")
    private final String category;

    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.01", message = "Unit price must be greater than 0")
    private final BigDecimal unitPrice;

    @Min(value = 1, message = "Quantity must be at least 1")
    private final int quantity;

    private BillItemDto(String name, String category, BigDecimal unitPrice, int quantity) {
        this.name = name;
        this.category = category;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    public static BillItemDto of(String name, String category, BigDecimal unitPrice, int quantity) {
        return new BillItemDto(name, category, unitPrice, quantity);
    }

    public static BillItemDto of(String name, String category, double unitPrice, int quantity) {
        return new BillItemDto(name, category, BigDecimal.valueOf(unitPrice), quantity);
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BillItemDto that = (BillItemDto) o;
        return quantity == that.quantity &&
                Objects.equals(name, that.name) &&
                Objects.equals(category, that.category) &&
                Objects.equals(unitPrice, that.unitPrice);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, category, unitPrice, quantity);
    }

    @Override
    public String toString() {
        return String.format("BillItemDto{name='%s', category='%s', unitPrice=%s, quantity=%d}",
                name, category, unitPrice, quantity);
    }
}