package io.mosaed.retaildiscountservice.infrastructure.adapter.persistence.mongodb.repository;

/**
 *
 * @author MOSAED ALOTAIBI
 */

import io.mosaed.retaildiscountservice.application.port.out.BillRepository;
import io.mosaed.retaildiscountservice.application.port.out.CustomerRepository;
import io.mosaed.retaildiscountservice.domain.model.Bill;
import io.mosaed.retaildiscountservice.domain.model.Customer;
import io.mosaed.retaildiscountservice.infrastructure.adapter.persistence.mongodb.document.BillDocument;
import io.mosaed.retaildiscountservice.infrastructure.adapter.persistence.mongodb.mapper.PersistenceMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class MongoBillRepository implements BillRepository {

    private final SpringBillRepository springRepository;
    private final CustomerRepository customerRepository;

    public MongoBillRepository(
            SpringBillRepository springRepository,
            CustomerRepository customerRepository) {
        this.springRepository = springRepository;
        this.customerRepository = customerRepository;
    }

    @Override
    public Bill save(Bill bill) {
        // Convert to document and save
        BillDocument document = PersistenceMapper.toDocument(bill);
        BillDocument saved = springRepository.save(document);

        // Load the customer to reconstruct the domain Bill
        // This should always succeed because the Bill already has a Customer
        Customer customer = customerRepository.findById(saved.getCustomerId())
                .orElseThrow(() -> new IllegalStateException(
                        "Customer not found for saved bill: " + saved.getCustomerId()
                ));

        // Reconstruct the domain Bill with the customer
        return PersistenceMapper.toDomain(saved, customer);
    }

    @Override
    public Optional<Bill> findById(String billId) {
        return springRepository.findById(billId)
                .flatMap(document -> {
                    // Load the associated customer
                    Optional<Customer> customerOpt =
                            customerRepository.findById(document.getCustomerId());

                    // If customer doesn't exist, we can't reconstruct the Bill
                    // This indicates a data integrity problem
                    if (customerOpt.isEmpty()) {
                        // Could log a warning here about orphaned bill
                        return Optional.empty();
                    }

                    // Reconstruct the domain Bill with the customer
                    Bill bill = PersistenceMapper.toDomain(document, customerOpt.get());
                    return Optional.of(bill);
                });
    }

    @Override
    public boolean deleteById(String billId) {
        if (springRepository.existsById(billId)) {
            springRepository.deleteById(billId);
            return true;
        }
        return false;
    }

    @Override
    public boolean existsById(String billId) {
        return springRepository.existsById(billId);
    }

    @Override
    public List<Bill> findByCustomerId(String customerId) {
        List<BillDocument> documents = springRepository.findByCustomerId(customerId);
        return convertDocumentsToBills(documents);
    }

    @Override
    public List<Bill> findByCustomerIdAndDateRange(String customerId, LocalDateTime from, LocalDateTime to) {
        List<BillDocument> documents = springRepository.findByCustomerIdAndCreatedAtBetween(
                customerId, from, to);
        return convertDocumentsToBills(documents);
    }

    @Override
    public List<Bill> findRecentBills(int limit) {
        PageRequest pageRequest = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<BillDocument> documents = springRepository.findAll(pageRequest).getContent();
        return convertDocumentsToBills(documents);
    }

    /**
     * Helper method to convert a list of BillDocuments to Bills.
     * Handles customer lookup for each bill.
     */
    private List<Bill> convertDocumentsToBills(List<BillDocument> documents) {
        return documents.stream()
                .map(document -> {
                    // Load the customer for this bill
                    Optional<Customer> customerOpt = customerRepository.findById(document.getCustomerId());

                    // Skip bills with missing customers (data integrity issue)
                    if (customerOpt.isEmpty()) {
                        return null;
                    }

                    return PersistenceMapper.toDomain(document, customerOpt.get());
                })
                .filter(bill -> bill != null) // Filter out nulls from missing customers
                .collect(Collectors.toList());
    }
}

interface SpringBillRepository extends MongoRepository<BillDocument, String> {
    // All standard CRUD methods are auto-implemented by Spring Data

    /**
     * Find all bills for a specific customer
     */
    List<BillDocument> findByCustomerId(String customerId);

    /**
     * Find bills for a customer within a date range
     */
    List<BillDocument> findByCustomerIdAndCreatedAtBetween(
            String customerId,
            LocalDateTime from,
            LocalDateTime to
    );
}
