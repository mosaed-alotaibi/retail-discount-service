package io.mosaed.retaildiscountservice.infrastructure.config;

/**
 *
 * @author MOSAED ALOTAIBI
 */

import io.mosaed.retaildiscountservice.application.port.out.CustomerRepository;
import io.mosaed.retaildiscountservice.domain.model.Customer;
import io.mosaed.retaildiscountservice.domain.model.CustomerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.LocalDate;

@Configuration
public class DataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    @Profile("!test")
    public CommandLineRunner initializeData(CustomerRepository customerRepository) {
        return args -> {
            logger.info("Initializing test customer data...");

            if (customerRepository.existsById("EMP001")) {
                logger.info("Test data already exists. Skipping initialization.");
                return;
            }

            Customer employee = Customer.of(
                    "EMP001",
                    CustomerType.EMPLOYEE,
                    LocalDate.now().minusYears(3) // Employee for 3 years
            );
            customerRepository.save(employee);
            logger.info("Created employee customer: EMP001 (30% discount)");

            Customer affiliate = Customer.of(
                    "AFF001",
                    CustomerType.AFFILIATE,
                    LocalDate.now().minusYears(2) // Affiliate for 2 years
            );
            customerRepository.save(affiliate);
            logger.info("Created affiliate customer: AFF001 (10% discount)");

            Customer longTermCustomer = Customer.of(
                    "CUST001",
                    CustomerType.REGULAR,
                    LocalDate.now().minusYears(3)
            );
            customerRepository.save(longTermCustomer);
            logger.info("Created long-term customer: CUST001 (5% discount - 3 years)");

            Customer regularCustomer = Customer.of(
                    "CUST002",
                    CustomerType.REGULAR,
                    LocalDate.now().minusMonths(6) // Only 6 months tenure
            );
            customerRepository.save(regularCustomer);
            logger.info("Created regular customer: CUST002 (0% discount - 6 months)");

            logger.info("Test customer data initialization complete!");
            logger.info("You can authenticate with any of these customers:");
            logger.info("  - Employee:    username=EMP001,  password=password (30% discount)");
            logger.info("  - Affiliate:   username=AFF001,  password=password (10% discount)");
            logger.info("  - Long-term:   username=CUST001, password=password (5% discount)");
            logger.info("  - Regular:     username=CUST002, password=password (0% discount)");
        };
    }
}
