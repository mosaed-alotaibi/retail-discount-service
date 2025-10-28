package io.mosaed.retaildiscountservice.domain.model;

/**
 *
 * @author MOSAED ALOTAIBI
 */

import io.mosaed.retaildiscountservice.domain.event.BillCalculated;
import io.mosaed.retaildiscountservice.domain.event.BillCreated;
import io.mosaed.retaildiscountservice.domain.event.DomainEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

public class Bill {

    private static final int BILL_BASED_DISCOUNT_THRESHOLD = 100;
    private static final int BILL_BASED_DISCOUNT_AMOUNT = 5;

    private final String billId;
    private final LocalDateTime createdAt;

    private final Customer customer;
    private final List<BillItem> items;

    // Domain events collected by this aggregate
    private final transient List<DomainEvent> domainEvents = new ArrayList<>();

    // cached calculation results (calc on demand)
    private transient DiscountBreakdown cachedDiscount;

    private Bill(String billId, Customer customer, List<BillItem> items, LocalDateTime createdAt) {
        this.billId = billId;
        this.customer = customer;
        this.items = new ArrayList<>(items);
        this.createdAt = createdAt;
    }

    public static Bill create(Customer customer, List<BillItem> items) {
        validateCreation(customer, items);
        Bill bill = new Bill(
                UUID.randomUUID().toString(),
                customer,
                items,
                LocalDateTime.now()
        );

        // Register domain event
        Money totalAmount = bill.getTotalAmount();
        bill.registerEvent(new BillCreated(
                bill.billId,
                customer.getCustomerId(),
                totalAmount.getAmount(),
                totalAmount.getAmount() // Initial net payable before calculation
        ));

        return bill;
    }

    public static Bill reconstitute(String billId, Customer customer, List<BillItem> items, LocalDateTime createdAt) {
        Objects.requireNonNull(billId, "Bill ID cannot be null");
        validateCreation(customer, items);
        Objects.requireNonNull(createdAt, "Created date cannot be null");
        return new Bill(billId, customer, items, createdAt);
    }

    private static void validateCreation(Customer customer, List<BillItem> items) {
        Objects.requireNonNull(customer, "Customer cannot be null");
        Objects.requireNonNull(items, "Items cannot be null");
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Bill must have at least one item");
        }
    }

    public Money getTotalAmount() {
        return items.stream()
                .map(BillItem::getTotalPrice)
                .reduce(Money.zero(), Money::add);
    }

    public Money getPercentageDiscountEligibleAmount() {
        return items.stream()
                .map(BillItem::getAmountEligibleForPercentageDiscount)
                .reduce(Money.zero(), Money::add);
    }

    public DiscountBreakdown calculateDiscount() {
        // use cached result if already calculted
        if (cachedDiscount != null) {
            return cachedDiscount;
        }

        Money totalAmount = getTotalAmount();

        // Step 1: Calculate percen-based discount
        Money percentageDiscount = calculatePercentageDiscount();

        // Step 2: Calculate bill-based discount (applies to total after percentage discount)
        Money amountAfterPercentageDiscount = totalAmount.subtract(percentageDiscount);
        Money billBasedDiscount = calculateBillBasedDiscount(amountAfterPercentageDiscount);

        // Step 3: Calculate final net payable
        Money totalDiscount = percentageDiscount.add(billBasedDiscount);
        Money netPayable = totalAmount.subtract(totalDiscount);

        // cache the result
        cachedDiscount = new DiscountBreakdown(
                totalAmount,
                percentageDiscount,
                billBasedDiscount,
                totalDiscount,
                netPayable,
                customer.getDiscountPercentage()
        );

        // Register domain event for calculation
        registerEvent(new BillCalculated(
                billId,
                customer.getCustomerId(),
                totalAmount.getAmount(),
                percentageDiscount.getAmount(),
                customer.getDiscountPercentage(),
                billBasedDiscount.getAmount(),
                totalDiscount.getAmount(),
                netPayable.getAmount()
        ));

        return cachedDiscount;
    }

    private Money calculatePercentageDiscount() {
        int customerDiscountPercentage = customer.getDiscountPercentage();

        if (customerDiscountPercentage == 0) {
            return Money.zero();
        }

        Money eligibleAmount = getPercentageDiscountEligibleAmount();
        return eligibleAmount.applyPercentageDiscount(customerDiscountPercentage);
    }

    private Money calculateBillBasedDiscount(Money amount) {
        int numberOfHundreds = amount.divideAndFloor(BILL_BASED_DISCOUNT_THRESHOLD);
        int discountAmount = numberOfHundreds * BILL_BASED_DISCOUNT_AMOUNT;
        return Money.of(BigDecimal.valueOf(discountAmount));
    }

    public Money getNetPayableAmount() {
        return calculateDiscount().getNetPayable();
    }

    public List<BillItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public String getBillId() {
        return billId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Register a domain event that occurred in this aggregate.
     */
    private void registerEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }

    /**
     * Get all domain events and clear them.
     * This follows the pattern where events are collected during the aggregate's
     * lifecycle and then published by the application service.
     */
    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = new ArrayList<>(this.domainEvents);
        this.domainEvents.clear();
        return events;
    }

    /**
     * Get domain events without clearing them.
     */
    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public static class DiscountBreakdown {
        private final Money totalAmount;
        private final Money percentageDiscount;
        private final Money billBasedDiscount;
        private final Money totalDiscount;
        private final Money netPayable;
        private final int percentageDiscountRate;

        public DiscountBreakdown(Money totalAmount, Money percentageDiscount,
                                 Money billBasedDiscount, Money totalDiscount,
                                 Money netPayable, int percentageDiscountRate) {
            this.totalAmount = totalAmount;
            this.percentageDiscount = percentageDiscount;
            this.billBasedDiscount = billBasedDiscount;
            this.totalDiscount = totalDiscount;
            this.netPayable = netPayable;
            this.percentageDiscountRate = percentageDiscountRate;
        }

        public Money getTotalAmount() {
            return totalAmount;
        }

        public Money getPercentageDiscount() {
            return percentageDiscount;
        }

        public Money getBillBasedDiscount() {
            return billBasedDiscount;
        }

        public Money getTotalDiscount() {
            return totalDiscount;
        }

        public Money getNetPayable() {
            return netPayable;
        }

        public int getPercentageDiscountRate() {
            return percentageDiscountRate;
        }

        @Override
        public String toString() {
            return String.format(
                    "DiscountBreakdown{total=%s, percentageDiscount=%s (%d%%), billBasedDiscount=%s, totalDiscount=%s, netPayable=%s}",
                    totalAmount, percentageDiscount, percentageDiscountRate, billBasedDiscount, totalDiscount, netPayable
            );
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bill bill = (Bill) o;
        return Objects.equals(billId, bill.billId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(billId);
    }

    @Override
    public String toString() {
        return String.format("Bill{id='%s', customer=%s, itemCount=%d, netPayable=%s}",
                billId, customer.getCustomerId(), items.size(), getNetPayableAmount());
    }
}
