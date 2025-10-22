package io.mosaed.retaildiscountservice.application.dto;

/**
 *
 * @author MOSAED ALOTAIBI
 */

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public final class BillCalculationResponse {

    private final String billId;
    private final String customerId;
    private final LocalDateTime calculatedAt;
    private final BigDecimal totalAmount;
    private final BigDecimal percentageDiscount;
    private final int percentageDiscountRate;
    private final BigDecimal billBasedDiscount;
    private final BigDecimal totalDiscount;
    private final BigDecimal netPayable;

    private BillCalculationResponse(
            String billId,
            String customerId,
            LocalDateTime calculatedAt,
            BigDecimal totalAmount,
            BigDecimal percentageDiscount,
            int percentageDiscountRate,
            BigDecimal billBasedDiscount,
            BigDecimal totalDiscount,
            BigDecimal netPayable) {
        this.billId = billId;
        this.customerId = customerId;
        this.calculatedAt = calculatedAt;
        this.totalAmount = totalAmount;
        this.percentageDiscount = percentageDiscount;
        this.percentageDiscountRate = percentageDiscountRate;
        this.billBasedDiscount = billBasedDiscount;
        this.totalDiscount = totalDiscount;
        this.netPayable = netPayable;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String billId;
        private String customerId;
        private LocalDateTime calculatedAt;
        private BigDecimal totalAmount;
        private BigDecimal percentageDiscount;
        private int percentageDiscountRate;
        private BigDecimal billBasedDiscount;
        private BigDecimal totalDiscount;
        private BigDecimal netPayable;

        public Builder billId(String billId) {
            this.billId = billId;
            return this;
        }

        public Builder customerId(String customerId) {
            this.customerId = customerId;
            return this;
        }

        public Builder calculatedAt(LocalDateTime calculatedAt) {
            this.calculatedAt = calculatedAt;
            return this;
        }

        public Builder totalAmount(BigDecimal totalAmount) {
            this.totalAmount = totalAmount;
            return this;
        }

        public Builder percentageDiscount(BigDecimal percentageDiscount) {
            this.percentageDiscount = percentageDiscount;
            return this;
        }

        public Builder percentageDiscountRate(int percentageDiscountRate) {
            this.percentageDiscountRate = percentageDiscountRate;
            return this;
        }

        public Builder billBasedDiscount(BigDecimal billBasedDiscount) {
            this.billBasedDiscount = billBasedDiscount;
            return this;
        }

        public Builder totalDiscount(BigDecimal totalDiscount) {
            this.totalDiscount = totalDiscount;
            return this;
        }

        public Builder netPayable(BigDecimal netPayable) {
            this.netPayable = netPayable;
            return this;
        }

        public BillCalculationResponse build() {
            Objects.requireNonNull(billId, "Bill ID is required");
            Objects.requireNonNull(customerId, "Customer ID is required");
            Objects.requireNonNull(calculatedAt, "Calculated date is required");
            Objects.requireNonNull(totalAmount, "Total amount is required");
            Objects.requireNonNull(netPayable, "Net payable is required");

            return new BillCalculationResponse(
                    billId,
                    customerId,
                    calculatedAt,
                    totalAmount,
                    percentageDiscount,
                    percentageDiscountRate,
                    billBasedDiscount,
                    totalDiscount,
                    netPayable
            );
        }
    }

    // Getters
    public String getBillId() {
        return billId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public LocalDateTime getCalculatedAt() {
        return calculatedAt;
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
        BillCalculationResponse that = (BillCalculationResponse) o;
        return percentageDiscountRate == that.percentageDiscountRate &&
                Objects.equals(billId, that.billId) &&
                Objects.equals(customerId, that.customerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(billId, customerId, percentageDiscountRate);
    }

    @Override
    public String toString() {
        return String.format("BillCalculationResponse{billId='%s', customerId='%s', netPayable=%s}",
                billId, customerId, netPayable);
    }
}
