package io.mosaed.retaildiscountservice.domain.event;

/**
 *
 * @author MOSAED ALOTAIBI
 */

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Domain event published when a bill discount calculation is completed.
 */
public final class BillCalculated implements DomainEvent {

    private final String billId;
    private final String customerId;
    private final BigDecimal totalAmount;
    private final BigDecimal percentageDiscount;
    private final int percentageDiscountRate;
    private final BigDecimal billBasedDiscount;
    private final BigDecimal totalDiscount;
    private final BigDecimal netPayable;
    private final LocalDateTime occurredOn;

    public BillCalculated(
            String billId,
            String customerId,
            BigDecimal totalAmount,
            BigDecimal percentageDiscount,
            int percentageDiscountRate,
            BigDecimal billBasedDiscount,
            BigDecimal totalDiscount,
            BigDecimal netPayable) {
        this.billId = Objects.requireNonNull(billId, "Bill ID cannot be null");
        this.customerId = Objects.requireNonNull(customerId, "Customer ID cannot be null");
        this.totalAmount = Objects.requireNonNull(totalAmount, "Total amount cannot be null");
        this.percentageDiscount = percentageDiscount;
        this.percentageDiscountRate = percentageDiscountRate;
        this.billBasedDiscount = billBasedDiscount;
        this.totalDiscount = totalDiscount;
        this.netPayable = Objects.requireNonNull(netPayable, "Net payable cannot be null");
        this.occurredOn = LocalDateTime.now();
    }

    @Override
    public LocalDateTime occurredOn() {
        return occurredOn;
    }

    @Override
    public String eventType() {
        return "BillCalculated";
    }

    public String getBillId() {
        return billId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public BigDecimal getPercentageDiscount() {
        return percentageDiscount;
    }

    public int getPercentageDiscountRate() {
        return percentageDiscountRate;
    }

    public BigDecimal getBillBasedDiscount() {
        return billBasedDiscount;
    }

    public BigDecimal getTotalDiscount() {
        return totalDiscount;
    }

    public BigDecimal getNetPayable() {
        return netPayable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BillCalculated that = (BillCalculated) o;
        return Objects.equals(billId, that.billId) &&
                Objects.equals(occurredOn, that.occurredOn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(billId, occurredOn);
    }

    @Override
    public String toString() {
        return String.format(
                "BillCalculated{billId='%s', customerId='%s', totalAmount=%s, totalDiscount=%s, netPayable=%s, occurredOn=%s}",
                billId, customerId, totalAmount, totalDiscount, netPayable, occurredOn);
    }
}
