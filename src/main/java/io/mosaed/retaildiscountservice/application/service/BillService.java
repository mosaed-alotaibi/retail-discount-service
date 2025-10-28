package io.mosaed.retaildiscountservice.application.service;

/**
 *
 * @author MOSAED ALOTAIBI
 */

import io.mosaed.retaildiscountservice.application.dto.BillCalculationResponse;
import io.mosaed.retaildiscountservice.application.dto.CalculateBillCommand;
import io.mosaed.retaildiscountservice.application.mapper.BillMapper;
import io.mosaed.retaildiscountservice.application.port.in.CalculateBillUseCase;
import io.mosaed.retaildiscountservice.application.port.in.GetBillUseCase;
import io.mosaed.retaildiscountservice.application.port.in.ListBillsUseCase;
import io.mosaed.retaildiscountservice.application.port.out.BillRepository;
import io.mosaed.retaildiscountservice.application.port.out.CustomerRepository;

import io.mosaed.retaildiscountservice.domain.exception.CustomerNotFoundException;
import io.mosaed.retaildiscountservice.domain.model.Bill;
import io.mosaed.retaildiscountservice.domain.model.BillItem;
import io.mosaed.retaildiscountservice.domain.model.Customer;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BillService implements CalculateBillUseCase, GetBillUseCase, ListBillsUseCase {

    private final CustomerRepository customerRepository;
    private final BillRepository billRepository;

    public BillService(
            CustomerRepository customerRepository,
            BillRepository billRepository) {
        this.customerRepository = customerRepository;
        this.billRepository = billRepository;
    }

    @Override
    public BillCalculationResponse execute(CalculateBillCommand command) {

        // Step 1: Retrieve the customer from persistence
        Customer customer = findCustomer(command.getCustomerId());

        // Step 2: Convert DTOs to domain objects
        List<BillItem> billItems = BillMapper.toDomainItems(command.getItems());

        // Step 3: Create the Bill aggregate
        Bill bill = Bill.create(customer, billItems);

        // Step 4: The domain calculates the discount
        bill.calculateDiscount(); // This populates the cached breakdown

        // Step 5: Persist the bill for audit trail
        Bill savedBill = billRepository.save(bill);

        // Step 6: Convert domain result back to DTO
        return BillMapper.toResponse(savedBill);
    }

    private Customer findCustomer(String customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));
    }

    // GetBillUseCase implementation
    @Override
    public Optional<BillCalculationResponse> execute(String billId) {
        return billRepository.findById(billId)
                .map(BillMapper::toResponse);
    }

    // ListBillsUseCase implementation
    @Override
    public List<BillCalculationResponse> findByCustomerId(String customerId) {
        return billRepository.findByCustomerId(customerId).stream()
                .map(BillMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<BillCalculationResponse> findByCustomerIdAndDateRange(
            String customerId,
            LocalDateTime from,
            LocalDateTime to) {
        return billRepository.findByCustomerIdAndDateRange(customerId, from, to).stream()
                .map(BillMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<BillCalculationResponse> findRecentBills(int limit) {
        return billRepository.findRecentBills(limit).stream()
                .map(BillMapper::toResponse)
                .collect(Collectors.toList());
    }
}