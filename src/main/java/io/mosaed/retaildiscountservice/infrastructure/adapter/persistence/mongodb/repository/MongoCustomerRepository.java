package io.mosaed.retaildiscountservice.infrastructure.adapter.persistence.mongodb.repository;

/**
 *
 * @author MOSAED ALOTAIBI
 */

import io.mosaed.retaildiscountservice.application.port.out.CustomerRepository;
import io.mosaed.retaildiscountservice.domain.model.Customer;
import io.mosaed.retaildiscountservice.infrastructure.adapter.persistence.mongodb.document.CustomerDocument;
import io.mosaed.retaildiscountservice.infrastructure.adapter.persistence.mongodb.mapper.PersistenceMapper;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * MongoDB implementation of the CustomerRepository output port.
 *
 * This is the ADAPTER in hexagonal architecture. It plugs into the port
 * (the CustomerRepository interface defined in application layer) and provides
 * a concrete implementation using MongoDB.
 *
 * HOW IT WORKS:
 *
 * 1. This class implements CustomerRepository (the port interface)
 * 2. It delegates to SpringCustomerRepository (Spring Data MongoDB interface)
 * 3. It uses PersistenceMapper to translate between domain and documents
 *
 * WHY this two-repository pattern?
 *
 * We have TWO repository interfaces:
 * - CustomerRepository (application layer): Domain-focused, uses Customer objects
 * - SpringCustomerRepository (infrastructure): Framework-focused, uses CustomerDocument
 *
 * This separation keeps Spring Data concerns out of the application layer.
 * The application layer doesn't know Spring Data exists - it just knows the
 * CustomerRepository interface. This makes the system more modular and testable.
 *
 * This pattern is sometimes called the "Repository Adapter" pattern or
 * "Anti-Corruption Repository" - it prevents Spring Data from leaking into
 * our clean architecture layers.
 */
@Repository
public class MongoCustomerRepository implements CustomerRepository {

    private final SpringCustomerRepository springRepository;

    /**
     * Constructor injection of Spring Data repository.
     *
     * Spring will automatically create an implementation of
     * SpringCustomerRepository at runtime because it extends MongoRepository.
     * That's the "magic" of Spring Data - you just declare the interface,
     * Spring implements it for you.
     */
    public MongoCustomerRepository(SpringCustomerRepository springRepository) {
        this.springRepository = springRepository;
    }

    /**
     * Find a customer by ID.
     *
     * FLOW:
     * 1. Ask Spring Data to find the CustomerDocument
     * 2. If found, convert it to domain Customer using mapper
     * 3. Return Optional<Customer> to the application layer
     *
     * The application layer gets a domain object, never sees MongoDB documents.
     */
    @Override
    public Optional<Customer> findById(String customerId) {
        return springRepository.findById(customerId)
                .map(PersistenceMapper::toDomain);
    }

    /**
     * Save a customer.
     *
     * FLOW:
     * 1. Convert domain Customer to CustomerDocument using mapper
     * 2. Save the document via Spring Data
     * 3. Convert the saved document back to domain Customer
     * 4. Return the domain Customer to the application layer
     *
     * WHY convert back after saving?
     * MongoDB might generate fields or modify data during save (though for
     * Customer it doesn't). By converting back, we ensure the returned object
     * reflects exactly what's in the database.
     */
    @Override
    public Customer save(Customer customer) {
        CustomerDocument document = PersistenceMapper.toDocument(customer);
        CustomerDocument saved = springRepository.save(document);
        return PersistenceMapper.toDomain(saved);
    }

    /**
     * Check if a customer exists.
     *
     * This delegates directly to Spring Data's existsById.
     * No mapping needed since we're just returning a boolean.
     *
     * This is more efficient than findById when you only need to check
     * existence - MongoDB can use an index-only scan without loading the
     * entire document.
     */
    @Override
    public boolean existsById(String customerId) {
        return springRepository.existsById(customerId);
    }
}

/**
 * Spring Data MongoDB repository interface.
 *
 * WHY a separate interface?
 *
 * This interface is in the infrastructure layer and knows about MongoDB
 * documents. The CustomerRepository interface in the application layer knows
 * about domain objects. We keep them separate to maintain clean boundaries.
 *
 * By extending MongoRepository, Spring Data automatically provides
 * implementations for: save, findById, existsById, delete, count, and more.
 * We don't write any SQL or MongoDB queries - Spring generates them from
 * method names.
 *
 * This interface doesn't need @Repository annotation because it's just an
 * interface that Spring Data implements. The annotation goes on our adapter
 * class (MongoCustomerRepository) instead.
 */
interface SpringCustomerRepository extends MongoRepository<CustomerDocument, String> {
    // Spring Data MongoDB automatically implements:
    // - Optional<CustomerDocument> findById(String id)
    // - CustomerDocument save(CustomerDocument document)
    // - boolean existsById(String id)
    // - void deleteById(String id)
    // - long count()
    // ... and many more

    // We can add custom query methods here if needed, like:
    // List<CustomerDocument> findByCustomerType(String type);
    // List<CustomerDocument> findByRegistrationDateAfter(LocalDate date);

    // Spring Data derives the MongoDB query from the method name!
}