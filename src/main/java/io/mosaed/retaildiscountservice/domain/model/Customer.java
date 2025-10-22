package io.mosaed.retaildiscountservice.domain.model;

/**
 *
 * @author MOSAED ALOTAIBI
 */


import java.time.LocalDate;
import java.time.Period;
import java.util.Objects;

public final class Customer {

    private static final int LONG_TERM_CUSTOMER_YEARS = 2;

    private final String customerId;
    private final CustomerType explicitType; // EMPLOYEE or AFFILIATE or REGULAR
    private final LocalDate registrationDate;

    private Customer(String customerId, CustomerType explicitType, LocalDate registrationDate) {
        this.customerId = customerId;
        this.explicitType = explicitType;
        this.registrationDate = registrationDate;
    }

    public static Customer of(String customerId, CustomerType explicitType, LocalDate registrationDate) {
        validateInputs(customerId, explicitType, registrationDate);
        return new Customer(customerId, explicitType, registrationDate);
    }

    private static void validateInputs(String customerId, CustomerType type, LocalDate registrationDate) {
        Objects.requireNonNull(customerId, "Customer ID cannot be null");
        Objects.requireNonNull(type, "Customer type cannot be null");
        Objects.requireNonNull(registrationDate, "Registration date cannot be null");

        if (customerId.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer ID cannot be empty");
        }
        if (registrationDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Registration date cannot be in the future");
        }
    }


    public CustomerType getEffectiveType() {
        // Employees and affiliates get their discount regardless of tenure
        if (explicitType == CustomerType.EMPLOYEE || explicitType == CustomerType.AFFILIATE) {
            return explicitType;
        }

        // Regular customers who have been around for 2+ years get long-term discount
        if (isLongTermCustomer()) {
            return CustomerType.LONG_TERM_CUSTOMER;
        }

        // Otherwise, just regular customer
        return CustomerType.REGULAR;
    }

    /**
     * Check if customer has been registered for 2+ years
     */
    private boolean isLongTermCustomer() {
        Period period = Period.between(registrationDate, LocalDate.now());
        return period.getYears() >= LONG_TERM_CUSTOMER_YEARS;
    }

    /**
     * Get the percentage discount this customer is entitled to
     */
    public int getDiscountPercentage() {
        return getEffectiveType().getDiscountPercentage();
    }

    /**
     * Get the number of years this customer has been registered
     */
    public int getYearsAsCustomer() {
        return Period.between(registrationDate, LocalDate.now()).getYears();
    }

    // Getters
    public String getCustomerId() {
        return customerId;
    }

    public CustomerType getExplicitType() {
        return explicitType;
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Customer customer = (Customer) o;
        return Objects.equals(customerId, customer.customerId) &&
                explicitType == customer.explicitType &&
                Objects.equals(registrationDate, customer.registrationDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customerId, explicitType, registrationDate);
    }

    @Override
    public String toString() {
        return String.format("Customer{id='%s', effectiveType=%s, yearsAsCustomer=%d}",
                customerId, getEffectiveType(), getYearsAsCustomer());
    }
}