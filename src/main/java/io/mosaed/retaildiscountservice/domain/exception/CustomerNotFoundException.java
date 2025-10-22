package io.mosaed.retaildiscountservice.domain.exception;

/**
 *
 * @author MOSAED ALOTAIBI
 */

public class CustomerNotFoundException extends DomainException {

    private final String customerId;

    public CustomerNotFoundException(String customerId) {
        super(String.format("Customer not found with ID: %s", customerId));
        this.customerId = customerId;
    }

    public String getCustomerId() {
        return customerId;
    }
}

