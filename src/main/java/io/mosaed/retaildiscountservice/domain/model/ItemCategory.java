package io.mosaed.retaildiscountservice.domain.model;

/**
 *
 * @author MOSAED ALOTAIBI
 */

public enum ItemCategory {

    GROCERY(false),

    ELECTRONICS(true),

    CLOTHING(true),

    HOME_GOODS(true),

    OTHER(true);

    private final boolean eligibleForPercentageDiscount;

    ItemCategory(boolean eligibleForPercentageDiscount) {
        this.eligibleForPercentageDiscount = eligibleForPercentageDiscount;
    }

    public boolean isEligibleForPercentageDiscount() {
        return eligibleForPercentageDiscount;
    }
}
