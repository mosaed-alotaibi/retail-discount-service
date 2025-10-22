package io.mosaed.retaildiscountservice.infrastructure.adapter.rest.persistence.mongodb.repository;

/**
 *
 * @author MOSAED ALOTAIBI
 */

import io.mosaed.retaildiscountservice.domain.model.Customer;
import io.mosaed.retaildiscountservice.domain.model.CustomerType;
import io.mosaed.retaildiscountservice.application.port.out.CustomerRepository;
import io.mosaed.retaildiscountservice.infrastructure.adapter.persistence.mongodb.repository.MongoCustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for CustomerRepository with real MongoDB.
 *
 * TESTING STRATEGY:
 *
 * Integration tests verify that components work together correctly with
 * real dependencies. Here we test:
 * - Spring Data MongoDB configuration
 * - Document mapping (domain ↔ MongoDB)
 * - Actual database operations (save, find, etc.)
 *
 * WHY TESTCONTAINERS?
 *
 * Testcontainers spins up a real MongoDB instance in a Docker container
 * specifically for testing. Benefits:
 * - Tests run against real MongoDB, not an in-memory fake
 * - Catches integration issues that unit tests can't
 * - Container is automatically started before tests and stopped after
 * - Each test run gets a fresh database (isolation)
 *
 * ANNOTATIONS EXPLAINED:
 *
 * @DataMongoTest - Configures only the MongoDB components (not full Spring Boot)
 * @Testcontainers - Enables Testcontainers support
 * @Container - Marks the container to be managed by Testcontainers
 * @Import - Imports our repository adapter into the test context
 *
 * @DynamicPropertySource - Dynamically configures MongoDB URI to point to
 * the Testcontainer instance instead of the default localhost:27017
 */
@DataMongoTest
@Testcontainers
@Import(MongoCustomerRepository.class)
@DisplayName("CustomerRepository Integration Tests")
class CustomerRepositoryIntegrationTest {

    /**
     * MongoDB container for testing.
     *
     * This creates a MongoDB container using the official mongo:5.0 image.
     * The @Container annotation tells Testcontainers to manage its lifecycle:
     * - Start before tests run
     * - Stop after tests complete
     * - Reuse across test methods in this class (faster)
     */
    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:5.0");

    /**
     * Configure Spring to use the Testcontainer MongoDB instance.
     *
     * This method runs before Spring creates the application context.
     * It dynamically sets the MongoDB URI to point to the container's
     * exposed port instead of localhost:27017.
     */
    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    /**
     * The repository under test.
     *
     * Spring injects our MongoCustomerRepository implementation.
     * This is the REAL repository talking to REAL MongoDB (in container).
     */
    @Autowired
    private CustomerRepository customerRepository;

    @Test
    @DisplayName("Should save and retrieve customer successfully")
    void shouldSaveAndRetrieveCustomerSuccessfully() {
        // Given a new customer
        Customer customer = Customer.of(
                "TEST001",
                CustomerType.EMPLOYEE,
                LocalDate.now().minusYears(1)
        );

        // When saving to database
        Customer saved = customerRepository.save(customer);

        // Then it should be persisted
        assertThat(saved).isNotNull();
        assertThat(saved.getCustomerId()).isEqualTo("TEST001");

        // And we should be able to retrieve it
        Optional<Customer> retrieved = customerRepository.findById("TEST001");

        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getCustomerId()).isEqualTo("TEST001");
        assertThat(retrieved.get().getExplicitType()).isEqualTo(CustomerType.EMPLOYEE);
        assertThat(retrieved.get().getRegistrationDate()).isEqualTo(customer.getRegistrationDate());
    }

    @Test
    @DisplayName("Should return empty when customer not found")
    void shouldReturnEmptyWhenCustomerNotFound() {
        // When searching for non-existent customer
        Optional<Customer> result = customerRepository.findById("NONEXISTENT");

        // Then should return empty
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should check customer existence correctly")
    void shouldCheckCustomerExistenceCorrectly() {
        // Given a saved customer
        Customer customer = Customer.of(
                "TEST002",
                CustomerType.AFFILIATE,
                LocalDate.now()
        );
        customerRepository.save(customer);

        // When checking existence
        boolean exists = customerRepository.existsById("TEST002");
        boolean notExists = customerRepository.existsById("NOTHERE");

        // Then results should be correct
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Should update existing customer")
    void shouldUpdateExistingCustomer() {
        // Given an existing customer
        Customer original = Customer.of(
                "TEST003",
                CustomerType.REGULAR,
                LocalDate.now().minusMonths(6)
        );
        customerRepository.save(original);

        // When updating (re-saving with same ID but different data)
        Customer updated = Customer.of(
                "TEST003",
                CustomerType.EMPLOYEE,  // Changed type
                LocalDate.now().minusYears(2)  // Changed date
        );
        customerRepository.save(updated);

        // Then the changes should be persisted
        Optional<Customer> retrieved = customerRepository.findById("TEST003");

        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getExplicitType()).isEqualTo(CustomerType.EMPLOYEE);
        assertThat(retrieved.get().getRegistrationDate()).isEqualTo(updated.getRegistrationDate());
    }

    @Test
    @DisplayName("Should handle all customer types correctly")
    void shouldHandleAllCustomerTypesCorrectly() {
        // Given customers of all types
        Customer employee = Customer.of("EMP", CustomerType.EMPLOYEE, LocalDate.now());
        Customer affiliate = Customer.of("AFF", CustomerType.AFFILIATE, LocalDate.now());
        Customer regular = Customer.of("REG", CustomerType.REGULAR, LocalDate.now());

        // When saving all types
        customerRepository.save(employee);
        customerRepository.save(affiliate);
        customerRepository.save(regular);

        // Then all should be retrievable with correct types
        assertThat(customerRepository.findById("EMP").get().getExplicitType())
                .isEqualTo(CustomerType.EMPLOYEE);
        assertThat(customerRepository.findById("AFF").get().getExplicitType())
                .isEqualTo(CustomerType.AFFILIATE);
        assertThat(customerRepository.findById("REG").get().getExplicitType())
                .isEqualTo(CustomerType.REGULAR);
    }

    @Test
    @DisplayName("Should preserve registration date precisely")
    void shouldPreserveRegistrationDatePrecisely() {
        // Given a specific registration date
        LocalDate specificDate = LocalDate.of(2020, 6, 15);
        Customer customer = Customer.of("TEST004", CustomerType.REGULAR, specificDate);

        // When saving and retrieving
        customerRepository.save(customer);
        Customer retrieved = customerRepository.findById("TEST004").get();

        // Then date should be preserved exactly
        assertThat(retrieved.getRegistrationDate()).isEqualTo(specificDate);
    }

    @Test
    @DisplayName("Should handle long-term customer calculation after retrieval")
    void shouldHandleLongTermCustomerCalculationAfterRetrieval() {
        // Given a customer registered 3 years ago
        Customer customer = Customer.of(
                "TEST005",
                CustomerType.REGULAR,
                LocalDate.now().minusYears(3)
        );

        // When saving and retrieving
        customerRepository.save(customer);
        Customer retrieved = customerRepository.findById("TEST005").get();

        // Then effective type should be calculated correctly
        assertThat(retrieved.getExplicitType()).isEqualTo(CustomerType.REGULAR);
        assertThat(retrieved.getEffectiveType()).isEqualTo(CustomerType.LONG_TERM_CUSTOMER);
        assertThat(retrieved.getDiscountPercentage()).isEqualTo(5);
    }
}
