package io.mosaed.retaildiscountservice.infrastructure.adapter.persistence.mongodb.document;

/**
 *
 * @author MOSAED ALOTAIBI
 */

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.CompoundIndex;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Document(collection = "bills")
@CompoundIndex(name = "customer_created_idx", def = "{'customer_id': 1, 'created_at': -1}")
public class BillDocument {

    @Id
    private String billId;

    @Field("customer_id")
    @Indexed
    private String customerId;

    @Field("customer_type")
    private String customerType;

    @Field("items")
    private List<BillItemDocument> items = new ArrayList<>();

    @Field("created_at")
    @Indexed
    private LocalDateTime createdAt;

    @Field("total_amount")
    private BigDecimal totalAmount;

    @Field("percentage_discount")
    private BigDecimal percentageDiscount;

    @Field("percentage_discount_rate")
    private int percentageDiscountRate;

    @Field("bill_based_discount")
    private BigDecimal billBasedDiscount;

    @Field("total_discount")
    private BigDecimal totalDiscount;

    @Field("net_payable")
    private BigDecimal netPayable;

    public BillDocument() {
    }

    public BillDocument(String billId, String customerId, String customerType,
                        List<BillItemDocument> items, LocalDateTime createdAt,
                        BigDecimal totalAmount, BigDecimal percentageDiscount,
                        int percentageDiscountRate, BigDecimal billBasedDiscount,
                        BigDecimal totalDiscount, BigDecimal netPayable) {
        this.billId = billId;
        this.customerId = customerId;
        this.customerType = customerType;
        this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
        this.createdAt = createdAt;
        this.totalAmount = totalAmount;
        this.percentageDiscount = percentageDiscount;
        this.percentageDiscountRate = percentageDiscountRate;
        this.billBasedDiscount = billBasedDiscount;
        this.totalDiscount = totalDiscount;
        this.netPayable = netPayable;
    }

    public String getBillId() {
        return billId;
    }

    public void setBillId(String billId) {
        this.billId = billId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerType() {
        return customerType;
    }

    public void setCustomerType(String customerType) {
        this.customerType = customerType;
    }

    public List<BillItemDocument> getItems() {
        return items;
    }

    public void setItems(List<BillItemDocument> items) {
        this.items = items;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getPercentageDiscount() {
        return percentageDiscount;
    }

    public void setPercentageDiscount(BigDecimal percentageDiscount) {
        this.percentageDiscount = percentageDiscount;
    }

    public int getPercentageDiscountRate() {
        return percentageDiscountRate;
    }

    public void setPercentageDiscountRate(int percentageDiscountRate) {
        this.percentageDiscountRate = percentageDiscountRate;
    }

    public BigDecimal getBillBasedDiscount() {
        return billBasedDiscount;
    }

    public void setBillBasedDiscount(BigDecimal billBasedDiscount) {
        this.billBasedDiscount = billBasedDiscount;
    }

    public BigDecimal getTotalDiscount() {
        return totalDiscount;
    }

    public void setTotalDiscount(BigDecimal totalDiscount) {
        this.totalDiscount = totalDiscount;
    }

    public BigDecimal getNetPayable() {
        return netPayable;
    }

    public void setNetPayable(BigDecimal netPayable) {
        this.netPayable = netPayable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BillDocument that = (BillDocument) o;
        return Objects.equals(billId, that.billId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(billId);
    }

    @Override
    public String toString() {
        return String.format("BillDocument{id='%s', customerId='%s', itemCount=%d, netPayable=%s}",
                billId, customerId, items.size(), netPayable);
    }
}
