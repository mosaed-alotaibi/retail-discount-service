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
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * MongoDB implementation of the BillRepository output port.
 *
 * This is more complex than CustomerRepository because Bill aggregates
 * reference Customers. When we load a Bill from MongoDB, we need to
 * reconstruct the full domain aggregate including the Customer value object.
 *
 * DESIGN DECISION: How to handle Customer reference?
 *
 * We have several options:
 *
 * Option 1: Store full customer data in every bill (embedding)
 * - Pro: Fast reads, no additional query needed
 * - Con: Data duplication, inconsistency if customer changes
 *
 * Option 2: Store only customer ID and load customer separately (referencing)
 * - Pro: No duplication, consistent data
 * - Con: Requires two database queries to load a bill
 *
 * Option 3: Store customer ID + denormalized key fields (hybrid)
 * - Pro: Fast reads with minimal duplication
 * - Con: Some denormalization, but only immutable fields
 *
 * We chose Option 3: Store customer ID and type (immutable fields) in the
 * bill document, but load the full Customer from the repository when
 * reconstructing the domain Bill. This balances performance and consistency.
 *
 * For this assessment, we'll actually use Option 2 (clean separation) because
 * it best demonstrates the repository pattern and keeps concerns separated.
 */
@Repository
public class MongoBillRepository implements BillRepository {

    private final SpringBillRepository springRepository;
    private final CustomerRepository customerRepository;

    /**
     * Constructor injection of dependencies.
     *
     * Notice we inject CustomerRepository, not the Spring Data repository.
     * This keeps our adapter decoupled from implementation details. We use
     * the domain-focused repository interface defined in the application layer.
     *
     * WHY does a repository depend on another repository?
     *
     * In DDD, aggregates can reference other aggregates by ID. When we
     * reconstitute a Bill aggregate, we need to load the associated Customer.
     * The BillRepository coordinates this, hiding the complexity from the
     * application layer.
     *
     * This is sometimes called the "Repository Coordination" pattern.
     */
    public MongoBillRepository(
            SpringBillRepository springRepository,
            CustomerRepository customerRepository) {
        this.springRepository = springRepository;
        this.customerRepository = customerRepository;
    }

    /**
     * Save a bill.
     *
     * FLOW:
     * 1. Convert domain Bill (with embedded Customer) to BillDocument
     * 2. Save document via Spring Data
     * 3. Load the Customer (to reconstruct domain object)
     * 4. Convert saved document back to domain Bill
     * 5. Return domain Bill
     *
     * Notice we're loading the Customer even though we just saved the bill.
     * This is because the Bill domain object requires a full Customer value
     * object, not just an ID. We need to be consistent with how we load bills.
     */
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

    /**
     * Find a bill by ID.
     *
     * FLOW:
     * 1. Load BillDocument from MongoDB
     * 2. Extract customer ID from document
     * 3. Load Customer from customer repository
     * 4. Convert document + customer to domain Bill
     * 5. Return Optional<Bill>
     *
     * WHY the complex flow?
     *
     * The domain Bill aggregate includes a Customer value object. When we load
     * a bill from storage, we need to reconstruct the complete aggregate. This
     * means loading the referenced customer and embedding it in the Bill.
     *
     * This is transparent to the application layer - it just calls findById
     * and gets a fully formed Bill aggregate. The complexity is hidden in
     * the adapter.
     *
     * If the customer doesn't exist (data integrity issue), we return empty
     * Optional rather than throwing an exception. This makes the method's
     * contract consistent - it returns empty when the bill can't be loaded,
     * whether because the bill doesn't exist or because of referential
     * integrity problems.
     */
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

    /**
     * Delete a bill by ID.
     *
     * This delegates directly to Spring Data. No mapping needed since we're
     * working with IDs and returning a boolean.
     *
     * In a production system, you might want soft deletes (marking records
     * as deleted rather than removing them) for audit purposes. But for the
     * assessment, hard deletes are fine.
     */
    @Override
    public boolean deleteById(String billId) {
        if (springRepository.existsById(billId)) {
            springRepository.deleteById(billId);
            return true;
        }
        return false;
    }

    /**
     * Check if a bill exists.
     *
     * Delegates to Spring Data's existsById for efficient existence checking.
     */
    @Override
    public boolean existsById(String billId) {
        return springRepository.existsById(billId);
    }
}

/**
 * Spring Data MongoDB repository interface for bills.
 *
 * Spring Data automatically implements this interface with full CRUD
 * functionality plus custom query methods we could add.
 *
 * Potential custom queries we could add for a production system:
 * - findByCustomerId(String customerId): Find all bills for a customer
 * - findByCreatedAtBetween(LocalDateTime start, LocalDateTime end): Date range queries
 * - findByNetPayableGreaterThan(BigDecimal amount): High-value bill queries
 * - findTop10ByOrderByCreatedAtDesc(): Recent bills
 *
 * Spring Data derives MongoDB queries from method names automatically!
 */
interface SpringBillRepository extends MongoRepository<BillDocument, String> {
    // All standard CRUD methods are auto-implemented by Spring Data

    // We could add custom queries here if needed for the assessment, like:
    // List<BillDocument> findByCustomerId(String customerId);
    //
    // Spring would automatically implement this as:
    // db.bills.find({ customer_id: customerId })
}
