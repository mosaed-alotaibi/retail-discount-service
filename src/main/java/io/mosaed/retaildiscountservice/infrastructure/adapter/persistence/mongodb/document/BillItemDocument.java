package io.mosaed.retaildiscountservice.infrastructure.adapter.persistence.mongodb.document;

/**
 *
 * @author MOSAED ALOTAIBI
 */

import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.util.Objects;

public class BillItemDocument {

    @Field("name")
    private String name;

    @Field("category")
    private String category;

    @Field("unit_price")
    private BigDecimal unitPrice;

    @Field("quantity")
    private int quantity;

    public BillItemDocument() {
    }

    public BillItemDocument(String name, String category, BigDecimal unitPrice, int quantity) {
        this.name = name;
        this.category = category;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BillItemDocument that = (BillItemDocument) o;
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
        return String.format("BillItemDocument{name='%s', category='%s', unitPrice=%s, qty=%d}",
                name, category, unitPrice, quantity);
    }
}
