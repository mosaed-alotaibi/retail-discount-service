package io.mosaed.retaildiscountservice.application.dto;

/**
 *
 * @author MOSAED ALOTAIBI
 */

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Objects;

public final class CalculateBillCommand {

    @NotBlank(message = "Customer ID is required and cannot be blank")
    private final String customerId;

    @NotEmpty(message = "Bill must contain at least one item")
    @Valid
    private final List<BillItemDto> items;

    private CalculateBillCommand(String customerId, List<BillItemDto> items) {
        this.customerId = customerId;
        this.items = items;
    }

    public static CalculateBillCommand of(String customerId, List<BillItemDto> items) {
        Objects.requireNonNull(customerId, "Customer ID cannot be null");
        Objects.requireNonNull(items, "Items cannot be null");
        return new CalculateBillCommand(customerId, items);
    }

    public String getCustomerId() {
        return customerId;
    }

    public List<BillItemDto> getItems() {
        return items;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CalculateBillCommand that = (CalculateBillCommand) o;
        return Objects.equals(customerId, that.customerId) &&
                Objects.equals(items, that.items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customerId, items);
    }

    @Override
    public String toString() {
        return String.format("CalculateBillCommand{customerId='%s', itemCount=%d}",
                customerId, items.size());
    }
}