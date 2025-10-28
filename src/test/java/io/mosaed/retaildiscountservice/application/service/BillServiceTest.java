package io.mosaed.retaildiscountservice.application.service;

/**
 *
 * @author MOSAED ALOTAIBI
 */

import io.mosaed.retaildiscountservice.application.dto.BillCalculationResponse;
import io.mosaed.retaildiscountservice.application.dto.BillItemDto;
import io.mosaed.retaildiscountservice.application.dto.CalculateBillCommand;
import io.mosaed.retaildiscountservice.application.port.out.BillRepository;
import io.mosaed.retaildiscountservice.application.port.out.CustomerRepository;
import io.mosaed.retaildiscountservice.domain.exception.CustomerNotFoundException;
import io.mosaed.retaildiscountservice.domain.model.Customer;
import io.mosaed.retaildiscountservice.domain.model.CustomerType;
import io.mosaed.retaildiscountservice.domain.model.Bill;
import io.mosaed.retaildiscountservice.domain.model.BillItem;
import io.mosaed.retaildiscountservice.domain.model.Money;
import io.mosaed.retaildiscountservice.domain.model.ItemCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Nested;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BillService Application Layer Tests")
class BillServiceTest {

    // Mocked dependencies - these are the output ports
    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private BillRepository billRepository;

    // The service under test - using real implementation
    private BillService billService;

    // Test data
    private Customer testCustomer;

    @BeforeEach
    void setUp() {
        // Create the service with mocked dependencies
        // This is manual dependency injection for testing
        billService = new BillService(customerRepository, billRepository);

        // Create test customer (real domain object, not mocked)
        testCustomer = Customer.of(
                "EMP001",
                CustomerType.EMPLOYEE,
                LocalDate.now().minusYears(1)
        );
    }

    @Test
    @DisplayName("Should calculate bill successfully for existing customer")
    void shouldCalculateBillSuccessfullyForExistingCustomer() {
        // Given a valid command
        CalculateBillCommand command = CalculateBillCommand.of(
                "EMP001",
                List.of(
                        BillItemDto.of("Laptop", "ELECTRONICS", 1000.00, 1)
                )
        );

        // Mock repository behavior - customer exists
        when(customerRepository.findById("EMP001"))
                .thenReturn(Optional.of(testCustomer));

        // Mock repository behavior - bill saved successfully
        when(billRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0)); // Return the same bill

        // When executing the use case
        BillCalculationResponse response = billService.execute(command);

        // Then the response should be correct
        assertThat(response).isNotNull();
        assertThat(response.getBillId()).isNotNull();
        assertThat(response.getCustomerId()).isEqualTo("EMP001");
        assertThat(response.getTotalAmount()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(response.getPercentageDiscountRate()).isEqualTo(30);
        assertThat(response.getNetPayable()).isEqualByComparingTo(new BigDecimal("665.00"));

        // Verify interactions with mocks
        verify(customerRepository).findById("EMP001");
        verify(billRepository).save(any());

        // Verify no other unexpected interactions
        verifyNoMoreInteractions(customerRepository, billRepository);
    }

    @Test
    @DisplayName("Should throw CustomerNotFoundException when customer doesn't exist")
    void shouldThrowCustomerNotFoundExceptionWhenCustomerDoesNotExist() {
        // Given a command with non-existent customer
        CalculateBillCommand command = CalculateBillCommand.of(
                "NONEXISTENT",
                List.of(
                        BillItemDto.of("Laptop", "ELECTRONICS", 1000.00, 1)
                )
        );

        // Mock repository to return empty (customer not found)
        when(customerRepository.findById("NONEXISTENT"))
                .thenReturn(Optional.empty());

        // When executing the use case
        // Then should throw CustomerNotFoundException
        assertThatThrownBy(() -> billService.execute(command))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessageContaining("NONEXISTENT");

        // Verify customer was looked up
        verify(customerRepository).findById("NONEXISTENT");

        // Verify bill was NOT saved (because customer lookup failed)
        verify(billRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should handle bills with multiple items correctly")
    void shouldHandleBillsWithMultipleItemsCorrectly() {
        // Given a command with multiple items
        CalculateBillCommand command = CalculateBillCommand.of(
                "EMP001",
                List.of(
                        BillItemDto.of("Laptop", "ELECTRONICS", 1000.00, 1),
                        BillItemDto.of("Mouse", "ELECTRONICS", 50.00, 2),
                        BillItemDto.of("Milk", "GROCERY", 5.00, 3)
                )
        );

        // Mock customer exists
        when(customerRepository.findById("EMP001"))
                .thenReturn(Optional.of(testCustomer));

        // Mock save
        when(billRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When executing
        BillCalculationResponse response = billService.execute(command);

        // Then calculation should be correct
        // Total: $1000 + $100 + $15 = $1115
        // Percentage on $1100 (excluding grocery): $330
        // After percentage: $785
        // Bill-based on $785: $35
        // Net: $750
        assertThat(response.getTotalAmount()).isEqualByComparingTo(new BigDecimal("1115.00"));
        assertThat(response.getPercentageDiscount()).isEqualByComparingTo(new BigDecimal("330.00"));
        assertThat(response.getBillBasedDiscount()).isEqualByComparingTo(new BigDecimal("35.00"));
        assertThat(response.getNetPayable()).isEqualByComparingTo(new BigDecimal("750.00"));
    }

    @Test
    @DisplayName("Should use correct customer for bill calculation")
    void shouldUseCorrectCustomerForBillCalculation() {
        // Given different customer types
        Customer affiliate = Customer.of("AFF001", CustomerType.AFFILIATE, LocalDate.now());

        CalculateBillCommand command = CalculateBillCommand.of(
                "AFF001",
                List.of(BillItemDto.of("Laptop", "ELECTRONICS", 1000.00, 1))
        );

        // Mock affiliate customer
        when(customerRepository.findById("AFF001"))
                .thenReturn(Optional.of(affiliate));

        when(billRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When executing
        BillCalculationResponse response = billService.execute(command);

        // Then should use affiliate discount (10%, not employee 30%)
        assertThat(response.getPercentageDiscountRate()).isEqualTo(10);
        assertThat(response.getPercentageDiscount()).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("Should save bill to repository")
    void shouldSaveBillToRepository() {
        // Given a valid command
        CalculateBillCommand command = CalculateBillCommand.of(
                "EMP001",
                List.of(BillItemDto.of("Laptop", "ELECTRONICS", 1000.00, 1))
        );

        when(customerRepository.findById(anyString()))
                .thenReturn(Optional.of(testCustomer));

        when(billRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When executing
        billService.execute(command);

        // Then bill should be saved exactly once
        verify(billRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Should handle zero quantity items gracefully")
    void shouldHandleInvalidItemsGracefully() {
        // Given command with invalid item (zero quantity)
        CalculateBillCommand command = CalculateBillCommand.of(
                "EMP001",
                List.of(BillItemDto.of("Laptop", "ELECTRONICS", 1000.00, 0))
        );

        when(customerRepository.findById("EMP001"))
                .thenReturn(Optional.of(testCustomer));

        // When executing with invalid data
        // Then should throw IllegalArgumentException from domain
        assertThatThrownBy(() -> billService.execute(command))
                .isInstanceOf(IllegalArgumentException.class);

        // Bill should not be saved when domain validation fails
        verify(billRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should handle grocery-only bills correctly")
    void shouldHandleGroceryOnlyBillsCorrectly() {
        // Given bill with only groceries
        CalculateBillCommand command = CalculateBillCommand.of(
                "EMP001",
                List.of(
                        BillItemDto.of("Milk", "GROCERY", 5.00, 10),
                        BillItemDto.of("Bread", "GROCERY", 3.00, 5)
                )
        );

        when(customerRepository.findById("EMP001"))
                .thenReturn(Optional.of(testCustomer));

        when(billRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When executing
        BillCalculationResponse response = billService.execute(command);

        // Then no percentage discount (all groceries)
        // Total: $50 + $15 = $65
        // No percentage discount
        // No bill-based discount (under $100)
        // Net: $65
        assertThat(response.getPercentageDiscount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.getBillBasedDiscount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.getNetPayable()).isEqualByComparingTo(new BigDecimal("65.00"));
    }

    // ============================================
    // GetBillUseCase Tests
    // ============================================

    @Nested
    @DisplayName("GetBillUseCase Tests - Retrieve Bill by ID")
    class GetBillUseCaseTests {

        @Test
        @DisplayName("Should retrieve bill by ID successfully")
        void shouldRetrieveBillByIdSuccessfully() {
            // Given a bill exists
            Bill bill = Bill.create(
                    testCustomer,
                    List.of(BillItem.of("Laptop", ItemCategory.ELECTRONICS, Money.of(1000.00), 1))
            );
            bill.calculateDiscount();

            when(billRepository.findById(bill.getBillId()))
                    .thenReturn(Optional.of(bill));

            // When retrieving the bill
            Optional<BillCalculationResponse> response = billService.execute(bill.getBillId());

            // Then the bill should be returned
            assertThat(response).isPresent();
            assertThat(response.get().getBillId()).isEqualTo(bill.getBillId());
            assertThat(response.get().getCustomerId()).isEqualTo("EMP001");

            verify(billRepository).findById(bill.getBillId());
        }

        @Test
        @DisplayName("Should return empty when bill not found")
        void shouldReturnEmptyWhenBillNotFound() {
            // Given a bill doesn't exist
            when(billRepository.findById("NONEXISTENT"))
                    .thenReturn(Optional.empty());

            // When retrieving the bill
            Optional<BillCalculationResponse> response = billService.execute("NONEXISTENT");

            // Then should return empty
            assertThat(response).isEmpty();

            verify(billRepository).findById("NONEXISTENT");
        }
    }

    // ============================================
    // ListBillsUseCase Tests
    // ============================================

    @Nested
    @DisplayName("ListBillsUseCase Tests - Query Bills")
    class ListBillsUseCaseTests {

        private Bill bill1;
        private Bill bill2;

        @BeforeEach
        void setUpBills() {
            bill1 = Bill.create(
                    testCustomer,
                    List.of(BillItem.of("Laptop", ItemCategory.ELECTRONICS, Money.of(1000.00), 1))
            );
            bill1.calculateDiscount();

            bill2 = Bill.create(
                    testCustomer,
                    List.of(BillItem.of("Mouse", ItemCategory.ELECTRONICS, Money.of(50.00), 1))
            );
            bill2.calculateDiscount();
        }

        @Test
        @DisplayName("Should find bills by customer ID")
        void shouldFindBillsByCustomerId() {
            // Given customer has bills
            when(billRepository.findByCustomerId("EMP001"))
                    .thenReturn(Arrays.asList(bill1, bill2));

            // When finding bills
            List<BillCalculationResponse> bills = billService.findByCustomerId("EMP001");

            // Then should return all customer bills
            assertThat(bills).hasSize(2);
            assertThat(bills.get(0).getCustomerId()).isEqualTo("EMP001");
            assertThat(bills.get(1).getCustomerId()).isEqualTo("EMP001");

            verify(billRepository).findByCustomerId("EMP001");
        }

        @Test
        @DisplayName("Should return empty list when customer has no bills")
        void shouldReturnEmptyListWhenCustomerHasNoBills() {
            // Given customer has no bills
            when(billRepository.findByCustomerId("CUST001"))
                    .thenReturn(Collections.emptyList());

            // When finding bills
            List<BillCalculationResponse> bills = billService.findByCustomerId("CUST001");

            // Then should return empty list
            assertThat(bills).isEmpty();

            verify(billRepository).findByCustomerId("CUST001");
        }

        @Test
        @DisplayName("Should find bills by customer ID and date range")
        void shouldFindBillsByCustomerIdAndDateRange() {
            // Given customer has bills in date range
            LocalDateTime from = LocalDateTime.now().minusDays(7);
            LocalDateTime to = LocalDateTime.now();

            when(billRepository.findByCustomerIdAndDateRange("EMP001", from, to))
                    .thenReturn(Arrays.asList(bill1, bill2));

            // When finding bills with date range
            List<BillCalculationResponse> bills = billService.findByCustomerIdAndDateRange("EMP001", from, to);

            // Then should return bills in date range
            assertThat(bills).hasSize(2);

            verify(billRepository).findByCustomerIdAndDateRange("EMP001", from, to);
        }

        @Test
        @DisplayName("Should find recent bills with limit")
        void shouldFindRecentBillsWithLimit() {
            // Given repository has bills
            when(billRepository.findRecentBills(10))
                    .thenReturn(Arrays.asList(bill1, bill2));

            // When finding recent bills
            List<BillCalculationResponse> bills = billService.findRecentBills(10);

            // Then should return limited bills
            assertThat(bills).hasSize(2);

            verify(billRepository).findRecentBills(10);
        }
    }
}