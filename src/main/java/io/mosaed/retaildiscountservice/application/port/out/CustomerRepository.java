package io.mosaed.retaildiscountservice.application.port.out;

/**
 *
 * @author MOSAED ALOTAIBI
 */

import io.mosaed.retaildiscountservice.domain.model.Customer;

import java.util.Optional;

public interface CustomerRepository {

    Optional<Customer> findById(String customerId);
    Customer save(Customer customer);
    boolean existsById(String customerId);
}
