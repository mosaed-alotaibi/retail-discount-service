package io.mosaed.retaildiscountservice.domain.model;

/**
 *
 * @author MOSAED ALOTAIBI
 */

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Money Value Object Tests")
class MoneyTest {

    @Test
    @DisplayName("Should create Money from BigDecimal")
    void shouldCreateMoneyFromBigDecimal() {
        // Given a BigDecimal amount
        BigDecimal amount = new BigDecimal("99.99");

        // When creating Money
        Money money = Money.of(amount);

        // Then the amount should be stored with proper scale
        assertThat(money.getAmount()).isEqualByComparingTo(new BigDecimal("99.99"));
    }

    @Test
    @DisplayName("Should create Money from double")
    void shouldCreateMoneyFromDouble() {
        // When creating Money from double
        Money money = Money.of(50.0);

        // Then it should convert correctly
        assertThat(money.getAmount()).isEqualByComparingTo(new BigDecimal("50.00"));
    }

    @Test
    @DisplayName("Should create zero Money")
    void shouldCreateZeroMoney() {
        // When creating zero Money
        Money money = Money.zero();

        // Then it should be zero
        assertThat(money.getAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(money.isZero()).isTrue();
    }

    @Test
    @DisplayName("Should reject null amount")
    void shouldRejectNullAmount() {
        // When creating Money with null
        // Then it should throw exception
        assertThatThrownBy(() -> Money.of((BigDecimal) null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Amount cannot be null");
    }

    @Test
    @DisplayName("Should reject negative amount")
    void shouldRejectNegativeAmount() {
        // When creating Money with negative amount
        // Then it should throw exception
        assertThatThrownBy(() -> Money.of(new BigDecimal("-10.00")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Amount cannot be negative");
    }

    @Test
    @DisplayName("Should add two Money values")
    void shouldAddTwoMoneyValues() {
        // Given two Money amounts
        Money first = Money.of(100.00);
        Money second = Money.of(50.50);

        // When adding them
        Money result = first.add(second);

        // Then the result should be correct
        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("150.50"));
    }

    @Test
    @DisplayName("Should subtract Money values")
    void shouldSubtractMoneyValues() {
        // Given two Money amounts
        Money first = Money.of(100.00);
        Money second = Money.of(30.00);

        // When subtracting
        Money result = first.subtract(second);

        // Then the result should be correct
        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("70.00"));
    }

    @Test
    @DisplayName("Should reject subtraction resulting in negative")
    void shouldRejectSubtractionResultingInNegative() {
        // Given amounts where subtraction would go negative
        Money smaller = Money.of(50.00);
        Money larger = Money.of(100.00);

        // When subtracting larger from smaller
        // Then it should throw exception
        assertThatThrownBy(() -> smaller.subtract(larger))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("negative amount");
    }

    @Test
    @DisplayName("Should multiply Money by factor")
    void shouldMultiplyMoneyByFactor() {
        // Given a Money amount
        Money money = Money.of(50.00);

        // When multiplying by factor
        Money result = money.multiply(new BigDecimal("2"));

        // Then the result should be correct
        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("Should apply percentage discount correctly")
    void shouldApplyPercentageDiscountCorrectly() {
        // Given a Money amount
        Money money = Money.of(1000.00);

        // When applying 30% discount
        Money discount = money.applyPercentageDiscount(30);

        // Then discount should be $300
        assertThat(discount.getAmount()).isEqualByComparingTo(new BigDecimal("300.00"));
    }

    @Test
    @DisplayName("Should handle zero percent discount")
    void shouldHandleZeroPercentDiscount() {
        // Given a Money amount
        Money money = Money.of(100.00);

        // When applying 0% discount
        Money discount = money.applyPercentageDiscount(0);

        // Then discount should be zero
        assertThat(discount.isZero()).isTrue();
    }

    @Test
    @DisplayName("Should reject invalid percentage")
    void shouldRejectInvalidPercentage() {
        // Given a Money amount
        Money money = Money.of(100.00);

        // When applying invalid percentage
        // Then it should throw exception
        assertThatThrownBy(() -> money.applyPercentageDiscount(101))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("between 0 and 100");

        assertThatThrownBy(() -> money.applyPercentageDiscount(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("between 0 and 100");
    }

    @Test
    @DisplayName("Should compare Money values correctly")
    void shouldCompareMoneyValuesCorrectly() {
        // Given Money amounts
        Money hundred = Money.of(100.00);
        Money fifty = Money.of(50.00);
        Money anotherHundred = Money.of(100.00);

        // Then comparisons should work
        assertThat(hundred.isGreaterThanOrEqual(fifty)).isTrue();
        assertThat(hundred.isGreaterThanOrEqual(anotherHundred)).isTrue();
        assertThat(fifty.isGreaterThanOrEqual(hundred)).isFalse();
    }

    @Test
    @DisplayName("Should divide and floor correctly for bill-based discount")
    void shouldDivideAndFloorCorrectly() {
        // Given various amounts
        Money amount990 = Money.of(990.00);
        Money amount150 = Money.of(150.00);
        Money amount99 = Money.of(99.00);

        // When dividing by 100 and flooring
        // Then it should return correct multiples
        assertThat(amount990.divideAndFloor(100)).isEqualTo(9);   // $990 / $100 = 9
        assertThat(amount150.divideAndFloor(100)).isEqualTo(1);   // $150 / $100 = 1
        assertThat(amount99.divideAndFloor(100)).isEqualTo(0);    // $99 / $100 = 0
    }

    @Test
    @DisplayName("Should implement value equality correctly")
    void shouldImplementValueEqualityCorrectly() {
        // Given Money objects with same amount
        Money first = Money.of(50.00);
        Money second = Money.of(50.00);
        Money different = Money.of(75.00);

        // Then they should be equal by value
        assertThat(first).isEqualTo(second);
        assertThat(first).isNotEqualTo(different);
        assertThat(first.hashCode()).isEqualTo(second.hashCode());
    }

    @Test
    @DisplayName("Should round to two decimal places")
    void shouldRoundToTwoDecimalPlaces() {
        // Given amount with many decimal places
        BigDecimal precise = new BigDecimal("99.9999");

        // When creating Money
        Money money = Money.of(precise);

        // Then it should round to 2 decimal places
        assertThat(money.getAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
    }
}
