package io.mosaed.retaildiscountservice.domain.model;

/**
 *
 * @author MOSAED ALOTAIBI
 */

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Bill Aggregate Root Tests")
class BillTest {

    // Test data builders for common scenarios
    private Customer createEmployee() {
        return Customer.of("EMP001", CustomerType.EMPLOYEE, LocalDate.now().minusYears(1));
    }

    private Customer createAffiliate() {
        return Customer.of("AFF001", CustomerType.AFFILIATE, LocalDate.now().minusYears(1));
    }

    private Customer createLongTermCustomer() {
        return Customer.of("CUST001", CustomerType.REGULAR, LocalDate.now().minusYears(3));
    }

    private Customer createRegularCustomer() {
        return Customer.of("CUST002", CustomerType.REGULAR, LocalDate.now().minusMonths(6));
    }

    private BillItem createElectronicsItem(double price) {
        return BillItem.of("Laptop", ItemCategory.ELECTRONICS, Money.of(price), 1);
    }

    private BillItem createGroceryItem(double price) {
        return BillItem.of("Milk", ItemCategory.GROCERY, Money.of(price), 1);
    }

    @Nested
    @DisplayName("Employee Discount Tests (30%)")
    class EmployeeDiscountTests {

        @Test
        @DisplayName("Employee should get 30% off electronics")
        void employeeShouldGet30PercentOffElectronics() {
            // Given employee and electronics item
            Customer employee = createEmployee();
            List<BillItem> items = List.of(createElectronicsItem(1000.00));

            // When creating bill
            Bill bill = Bill.create(employee, items);
            Bill.DiscountBreakdown breakdown = bill.calculateDiscount();

            // Then should get 30% discount on $1000 = $300
            // Plus $5 per $100 on remaining $700 = $35
            // Total discount = $335, Net = $665
            assertThat(breakdown.getTotalAmount().getAmount()).isEqualByComparingTo("1000.00");
            assertThat(breakdown.getPercentageDiscount().getAmount()).isEqualByComparingTo("300.00");
            assertThat(breakdown.getPercentageDiscountRate()).isEqualTo(30);
            assertThat(breakdown.getBillBasedDiscount().getAmount()).isEqualByComparingTo("35.00");
            assertThat(breakdown.getTotalDiscount().getAmount()).isEqualByComparingTo("335.00");
            assertThat(breakdown.getNetPayable().getAmount()).isEqualByComparingTo("665.00");
        }

        @Test
        @DisplayName("Employee should get NO percentage discount on groceries")
        void employeeShouldGetNoPercentageDiscountOnGroceries() {
            // Given employee and ONLY grocery items
            Customer employee = createEmployee();
            List<BillItem> items = List.of(createGroceryItem(200.00));

            // When creating bill
            Bill bill = Bill.create(employee, items);
            Bill.DiscountBreakdown breakdown = bill.calculateDiscount();

            // Then NO percentage discount (groceries excluded)
            // But still get $5 per $100 on full amount = $10
            assertThat(breakdown.getPercentageDiscount().isZero()).isTrue();
            assertThat(breakdown.getBillBasedDiscount().getAmount()).isEqualByComparingTo("10.00");
            assertThat(breakdown.getNetPayable().getAmount()).isEqualByComparingTo("190.00");
        }

        @Test
        @DisplayName("Employee discount should apply only to non-grocery items in mixed cart")
        void employeeDiscountShouldApplyOnlyToNonGroceryInMixedCart() {
            // Given employee with mixed cart
            Customer employee = createEmployee();
            List<BillItem> items = List.of(
                    createElectronicsItem(1000.00),  // 30% discount applies
                    createGroceryItem(200.00)        // NO percentage discount
            );

            // When creating bill
            Bill bill = Bill.create(employee, items);
            Bill.DiscountBreakdown breakdown = bill.calculateDiscount();

            // Then percentage discount only on electronics: $1000 × 30% = $300
            // After percentage: $1200 - $300 = $900
            // Bill-based: $900 / $100 = 9, × $5 = $45
            // Net: $1200 - $300 - $45 = $855
            assertThat(breakdown.getTotalAmount().getAmount()).isEqualByComparingTo("1200.00");
            assertThat(breakdown.getPercentageDiscount().getAmount()).isEqualByComparingTo("300.00");
            assertThat(breakdown.getBillBasedDiscount().getAmount()).isEqualByComparingTo("45.00");
            assertThat(breakdown.getNetPayable().getAmount()).isEqualByComparingTo("855.00");
        }
    }

    @Nested
    @DisplayName("Affiliate Discount Tests (10%)")
    class AffiliateDiscountTests {

        @Test
        @DisplayName("Affiliate should get 10% off electronics")
        void affiliateShouldGet10PercentOffElectronics() {
            // Given affiliate and electronics
            Customer affiliate = createAffiliate();
            List<BillItem> items = List.of(createElectronicsItem(1000.00));

            // When creating bill
            Bill bill = Bill.create(affiliate, items);
            Bill.DiscountBreakdown breakdown = bill.calculateDiscount();

            // Then 10% discount: $1000 × 10% = $100
            // After percentage: $900
            // Bill-based: $900 / $100 = 9, × $5 = $45
            // Net: $1000 - $100 - $45 = $855
            assertThat(breakdown.getPercentageDiscount().getAmount()).isEqualByComparingTo("100.00");
            assertThat(breakdown.getPercentageDiscountRate()).isEqualTo(10);
            assertThat(breakdown.getBillBasedDiscount().getAmount()).isEqualByComparingTo("45.00");
            assertThat(breakdown.getNetPayable().getAmount()).isEqualByComparingTo("855.00");
        }
    }

    @Nested
    @DisplayName("Long-Term Customer Discount Tests (5%)")
    class LongTermCustomerDiscountTests {

        @Test
        @DisplayName("Long-term customer should get 5% off electronics")
        void longTermCustomerShouldGet5PercentOffElectronics() {
            // Given long-term customer (2+ years)
            Customer longTerm = createLongTermCustomer();
            List<BillItem> items = List.of(createElectronicsItem(1000.00));

            // When creating bill
            Bill bill = Bill.create(longTerm, items);
            Bill.DiscountBreakdown breakdown = bill.calculateDiscount();

            // Then 5% discount: $1000 × 5% = $50
            // After percentage: $950
            // Bill-based: $950 / $100 = 9, × $5 = $45
            // Net: $1000 - $50 - $45 = $905
            assertThat(breakdown.getPercentageDiscount().getAmount()).isEqualByComparingTo("50.00");
            assertThat(breakdown.getPercentageDiscountRate()).isEqualTo(5);
            assertThat(breakdown.getBillBasedDiscount().getAmount()).isEqualByComparingTo("45.00");
            assertThat(breakdown.getNetPayable().getAmount()).isEqualByComparingTo("905.00");
        }
    }

    @Nested
    @DisplayName("Regular Customer Tests (0% percentage discount)")
    class RegularCustomerTests {

        @Test
        @DisplayName("Regular customer should get NO percentage discount")
        void regularCustomerShouldGetNoPercentageDiscount() {
            // Given new regular customer
            Customer regular = createRegularCustomer();
            List<BillItem> items = List.of(createElectronicsItem(1000.00));

            // When creating bill
            Bill bill = Bill.create(regular, items);
            Bill.DiscountBreakdown breakdown = bill.calculateDiscount();

            // Then NO percentage discount, only bill-based
            // Bill-based: $1000 / $100 = 10, × $5 = $50
            // Net: $1000 - $50 = $950
            assertThat(breakdown.getPercentageDiscount().isZero()).isTrue();
            assertThat(breakdown.getPercentageDiscountRate()).isEqualTo(0);
            assertThat(breakdown.getBillBasedDiscount().getAmount()).isEqualByComparingTo("50.00");
            assertThat(breakdown.getNetPayable().getAmount()).isEqualByComparingTo("950.00");
        }
    }

    @Nested
    @DisplayName("Bill-Based Discount Tests ($5 per $100)")
    class BillBasedDiscountTests {

        @Test
        @DisplayName("Should calculate bill-based discount correctly for $990")
        void shouldCalculateBillBasedDiscountFor990() {
            // Given bill of $990 (example from requirements)
            Customer regular = createRegularCustomer();
            List<BillItem> items = List.of(createElectronicsItem(990.00));

            // When creating bill
            Bill bill = Bill.create(regular, items);
            Bill.DiscountBreakdown breakdown = bill.calculateDiscount();

            // Then bill-based discount: $990 / $100 = 9, × $5 = $45
            assertThat(breakdown.getBillBasedDiscount().getAmount()).isEqualByComparingTo("45.00");
            assertThat(breakdown.getNetPayable().getAmount()).isEqualByComparingTo("945.00");
        }

        @Test
        @DisplayName("Should get zero bill-based discount for amount under $100")
        void shouldGetZeroBillBasedDiscountForAmountUnder100() {
            // Given bill under $100
            Customer regular = createRegularCustomer();
            List<BillItem> items = List.of(createElectronicsItem(99.00));

            // When creating bill
            Bill bill = Bill.create(regular, items);
            Bill.DiscountBreakdown breakdown = bill.calculateDiscount();

            // Then NO bill-based discount
            assertThat(breakdown.getBillBasedDiscount().isZero()).isTrue();
            assertThat(breakdown.getNetPayable().getAmount()).isEqualByComparingTo("99.00");
        }

        @Test
        @DisplayName("Bill-based discount should apply to amount AFTER percentage discount")
        void billBasedDiscountShouldApplyAfterPercentageDiscount() {
            // Given employee with $500 in electronics
            Customer employee = createEmployee();
            List<BillItem> items = List.of(createElectronicsItem(500.00));

            // When creating bill
            Bill bill = Bill.create(employee, items);
            Bill.DiscountBreakdown breakdown = bill.calculateDiscount();

            // Percentage discount: $500 × 30% = $150
            // After percentage: $500 - $150 = $350
            // Bill-based on $350: $350 / $100 = 3, × $5 = $15
            // Net: $500 - $150 - $15 = $335
            assertThat(breakdown.getPercentageDiscount().getAmount()).isEqualByComparingTo("150.00");
            assertThat(breakdown.getBillBasedDiscount().getAmount()).isEqualByComparingTo("15.00");
            assertThat(breakdown.getNetPayable().getAmount()).isEqualByComparingTo("335.00");
        }
    }

    @Nested
    @DisplayName("Bill Creation and Validation Tests")
    class BillCreationTests {

        @Test
        @DisplayName("Should create bill with valid data")
        void shouldCreateBillWithValidData() {
            // Given valid customer and items
            Customer customer = createEmployee();
            List<BillItem> items = List.of(createElectronicsItem(100.00));

            // When creating bill
            Bill bill = Bill.create(customer, items);

            // Then bill should be created with ID and timestamp
            assertThat(bill.getBillId()).isNotNull();
            assertThat(bill.getCreatedAt()).isNotNull();
            assertThat(bill.getCustomer()).isEqualTo(customer);
            assertThat(bill.getItems()).hasSize(1);
        }

        @Test
        @DisplayName("Should reject null customer")
        void shouldRejectNullCustomer() {
            // When creating bill with null customer
            // Then should throw exception
            assertThatThrownBy(() -> Bill.create(null, List.of(createElectronicsItem(100.00))))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Customer cannot be null");
        }

        @Test
        @DisplayName("Should reject null items")
        void shouldRejectNullItems() {
            // When creating bill with null items
            // Then should throw exception
            assertThatThrownBy(() -> Bill.create(createEmployee(), null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Items cannot be null");
        }

        @Test
        @DisplayName("Should reject empty items list")
        void shouldRejectEmptyItemsList() {
            // When creating bill with no items
            // Then should throw exception
            assertThatThrownBy(() -> Bill.create(createEmployee(), List.of()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Bill must have at least one item");
        }
    }

    @Test
    @DisplayName("Should handle multiple items correctly")
    void shouldHandleMultipleItemsCorrectly() {
        // Given multiple items
        Customer employee = createEmployee();
        List<BillItem> items = List.of(
                BillItem.of("Laptop", ItemCategory.ELECTRONICS, Money.of(1000.00), 1),
                BillItem.of("Mouse", ItemCategory.ELECTRONICS, Money.of(50.00), 2),
                BillItem.of("Bread", ItemCategory.GROCERY, Money.of(5.00), 3)
        );

        // When creating bill
        Bill bill = Bill.create(employee, items);
        Bill.DiscountBreakdown breakdown = bill.calculateDiscount();

        // Total: $1000 + $100 (2×$50) + $15 (3×$5) = $1115
        // Percentage eligible: $1100 (excludes $15 grocery)
        // Percentage discount: $1100 × 30% = $330
        // After percentage: $1115 - $330 = $785
        // Bill-based: $785 / $100 = 7, × $5 = $35
        // Net: $1115 - $330 - $35 = $750
        assertThat(breakdown.getTotalAmount().getAmount()).isEqualByComparingTo("1115.00");
        assertThat(breakdown.getPercentageDiscount().getAmount()).isEqualByComparingTo("330.00");
        assertThat(breakdown.getBillBasedDiscount().getAmount()).isEqualByComparingTo("35.00");
        assertThat(breakdown.getNetPayable().getAmount()).isEqualByComparingTo("750.00");
    }
}
