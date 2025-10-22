package io.mosaed.retaildiscountservice.domain.model;

/**
 *
 * @author MOSAED ALOTAIBI
 */

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Customer Value Object Tests")
class CustomerTest {

    @Test
    @DisplayName("Should create employee customer")
    void shouldCreateEmployeeCustomer() {
        // Given employee details
        String customerId = "EMP001";
        CustomerType type = CustomerType.EMPLOYEE;
        LocalDate registrationDate = LocalDate.now().minusYears(1);

        // When creating customer
        Customer customer = Customer.of(customerId, type, registrationDate);

        // Then customer should be created correctly
        assertThat(customer.getCustomerId()).isEqualTo(customerId);
        assertThat(customer.getExplicitType()).isEqualTo(CustomerType.EMPLOYEE);
        assertThat(customer.getEffectiveType()).isEqualTo(CustomerType.EMPLOYEE);
        assertThat(customer.getDiscountPercentage()).isEqualTo(30);
    }

    @Test
    @DisplayName("Employee should remain employee regardless of tenure")
    void employeeShouldRemainEmployeeRegardlessOfTenure() {
        // Given employee registered 3 years ago (would qualify for long-term)
        Customer employee = Customer.of(
                "EMP001",
                CustomerType.EMPLOYEE,
                LocalDate.now().minusYears(3)
        );

        // Then should still be employee, not long-term customer
        // Business rule: "A user can get only one of the percentage based discounts"
        assertThat(employee.getEffectiveType()).isEqualTo(CustomerType.EMPLOYEE);
        assertThat(employee.getDiscountPercentage()).isEqualTo(30);  // Not 5%
    }

    @Test
    @DisplayName("Affiliate should remain affiliate regardless of tenure")
    void affiliateShouldRemainAffiliateRegardlessOfTenure() {
        // Given affiliate registered 5 years ago
        Customer affiliate = Customer.of(
                "AFF001",
                CustomerType.AFFILIATE,
                LocalDate.now().minusYears(5)
        );

        // Then should still be affiliate
        assertThat(affiliate.getEffectiveType()).isEqualTo(CustomerType.AFFILIATE);
        assertThat(affiliate.getDiscountPercentage()).isEqualTo(10);  // Not 5%
    }

    @Test
    @DisplayName("Regular customer should become long-term after 2 years")
    void regularCustomerShouldBecomeLongTermAfterTwoYears() {
        // Given regular customer registered exactly 2 years ago
        Customer customer = Customer.of(
                "CUST001",
                CustomerType.REGULAR,
                LocalDate.now().minusYears(2)
        );

        // Then should be upgraded to long-term
        assertThat(customer.getExplicitType()).isEqualTo(CustomerType.REGULAR);
        assertThat(customer.getEffectiveType()).isEqualTo(CustomerType.LONG_TERM_CUSTOMER);
        assertThat(customer.getDiscountPercentage()).isEqualTo(5);
    }

    @Test
    @DisplayName("Regular customer should stay regular if less than 2 years")
    void regularCustomerShouldStayRegularIfLessThanTwoYears() {
        // Given regular customer registered 1 year and 11 months ago
        Customer customer = Customer.of(
                "CUST002",
                CustomerType.REGULAR,
                LocalDate.now().minusMonths(23)  // Just shy of 2 years
        );

        // Then should still be regular (no discount)
        assertThat(customer.getEffectiveType()).isEqualTo(CustomerType.REGULAR);
        assertThat(customer.getDiscountPercentage()).isEqualTo(0);
    }

    @Test
    @DisplayName("New regular customer should get zero discount")
    void newRegularCustomerShouldGetZeroDiscount() {
        // Given brand new customer
        Customer customer = Customer.of(
                "CUST003",
                CustomerType.REGULAR,
                LocalDate.now()
        );

        // Then should get no percentage discount
        assertThat(customer.getEffectiveType()).isEqualTo(CustomerType.REGULAR);
        assertThat(customer.getDiscountPercentage()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should calculate years as customer correctly")
    void shouldCalculateYearsAsCustomerCorrectly() {
        // Given customer registered 3 years ago
        Customer customer = Customer.of(
                "CUST004",
                CustomerType.REGULAR,
                LocalDate.now().minusYears(3)
        );

        // Then years calculation should be correct
        assertThat(customer.getYearsAsCustomer()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should reject null customer ID")
    void shouldRejectNullCustomerId() {
        // When creating customer with null ID
        // Then should throw exception
        assertThatThrownBy(() -> Customer.of(
                null,
                CustomerType.REGULAR,
                LocalDate.now()
        ))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Customer ID cannot be null");
    }

    @Test
    @DisplayName("Should reject empty customer ID")
    void shouldRejectEmptyCustomerId() {
        // When creating customer with empty ID
        // Then should throw exception
        assertThatThrownBy(() -> Customer.of(
                "   ",
                CustomerType.REGULAR,
                LocalDate.now()
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Customer ID cannot be empty");
    }

    @Test
    @DisplayName("Should reject null customer type")
    void shouldRejectNullCustomerType() {
        // When creating customer with null type
        // Then should throw exception
        assertThatThrownBy(() -> Customer.of(
                "CUST005",
                null,
                LocalDate.now()
        ))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Customer type cannot be null");
    }

    @Test
    @DisplayName("Should reject null registration date")
    void shouldRejectNullRegistrationDate() {
        // When creating customer with null registration date
        // Then should throw exception
        assertThatThrownBy(() -> Customer.of(
                "CUST006",
                CustomerType.REGULAR,
                null
        ))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Registration date cannot be null");
    }

    @Test
    @DisplayName("Should reject future registration date")
    void shouldRejectFutureRegistrationDate() {
        // When creating customer with future registration date
        // Then should throw exception
        assertThatThrownBy(() -> Customer.of(
                "CUST007",
                CustomerType.REGULAR,
                LocalDate.now().plusDays(1)
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Registration date cannot be in the future");
    }

    @Test
    @DisplayName("Should implement value equality correctly")
    void shouldImplementValueEqualityCorrectly() {
        // Given two customers with same attributes
        Customer first = Customer.of("CUST008", CustomerType.REGULAR, LocalDate.now());
        Customer second = Customer.of("CUST008", CustomerType.REGULAR, LocalDate.now());
        Customer different = Customer.of("CUST009", CustomerType.REGULAR, LocalDate.now());

        // Then they should be equal by value
        assertThat(first).isEqualTo(second);
        assertThat(first).isNotEqualTo(different);
        assertThat(first.hashCode()).isEqualTo(second.hashCode());
    }
}