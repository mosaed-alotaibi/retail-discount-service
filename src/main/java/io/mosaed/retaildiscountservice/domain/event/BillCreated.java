package io.mosaed.retaildiscountservice.domain.event;

/**
 *
 * @author MOSAED ALOTAIBI
 */

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Domain event published when a new bill is created.
 */
public final class BillCreated implements DomainEvent {

    private final String billId;
    private final String customerId;
    private final BigDecimal totalAmount;
    private final BigDecimal netPayable;
    private final LocalDateTime occurredOn;

    public BillCreated(String billId, String customerId, BigDecimal totalAmount, BigDecimal netPayable) {
        this.billId = Objects.requireNonNull(billId, "Bill ID cannot be null");
        this.customerId = Objects.requireNonNull(customerId, "Customer ID cannot be null");
        this.totalAmount = Objects.requireNonNull(totalAmount, "Total amount cannot be null");
        this.netPayable = Objects.requireNonNull(netPayable, "Net payable cannot be null");
        this.occurredOn = LocalDateTime.now();
    }

    @Override
    public LocalDateTime occurredOn() {
        return occurredOn;
    }

    @Override
    public String eventType() {
        return "BillCreated";
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

    public BigDecimal getNetPayable() {
        return netPayable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BillCreated that = (BillCreated) o;
        return Objects.equals(billId, that.billId) &&
                Objects.equals(occurredOn, that.occurredOn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(billId, occurredOn);
    }

    @Override
    public String toString() {
        return String.format("BillCreated{billId='%s', customerId='%s', totalAmount=%s, netPayable=%s, occurredOn=%s}",
                billId, customerId, totalAmount, netPayable, occurredOn);
    }
}
