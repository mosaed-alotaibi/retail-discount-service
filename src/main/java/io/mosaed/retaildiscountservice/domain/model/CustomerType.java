package io.mosaed.retaildiscountservice.domain.model;

/**
 *
 * @author MOSAED ALOTAIBI
 */

public enum CustomerType {

    EMPLOYEE(30),

    AFFILIATE(10),

    LONG_TERM_CUSTOMER(5),

    REGULAR(0);


    private final int discountPercentage;

    CustomerType(int discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    public int getDiscountPercentage() {
        return discountPercentage;
    }

    public boolean hasPercentageDiscount() {
        return discountPercentage > 0;
    }
}
