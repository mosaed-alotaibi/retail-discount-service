package io.mosaed.retaildiscountservice.application.mapper;

/**
 *
 * @author MOSAED ALOTAIBI
 */

import io.mosaed.retaildiscountservice.application.dto.BillItemDto;
import io.mosaed.retaildiscountservice.domain.exception.InvalidBillException;
import io.mosaed.retaildiscountservice.domain.model.BillItem;
import io.mosaed.retaildiscountservice.domain.model.ItemCategory;
import io.mosaed.retaildiscountservice.domain.model.Money;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("BillMapper Translation Tests")
class BillMapperTest {

    @Test
    @DisplayName("Should convert valid BillItemDto to domain BillItem")
    void shouldConvertValidBillItemDtoToDomain() {
        // Given a valid DTO
        BillItemDto dto = BillItemDto.of(
                "Laptop",
                "ELECTRONICS",
                new BigDecimal("1000.00"),
                1
        );

        // When converting to domain
        BillItem item = BillMapper.toDomainItem(dto);

        // Then conversion should be correct
        assertThat(item.getName()).isEqualTo("Laptop");
        assertThat(item.getCategory()).isEqualTo(ItemCategory.ELECTRONICS);
        assertThat(item.getUnitPrice()).isEqualTo(Money.of(1000.00));
        assertThat(item.getQuantity()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should convert list of DTOs to domain items")
    void shouldConvertListOfDtosToDomainItems() {
        // Given multiple DTOs
        List<BillItemDto> dtos = List.of(
                BillItemDto.of("Laptop", "ELECTRONICS", 1000.00, 1),
                BillItemDto.of("Mouse", "ELECTRONICS", 50.00, 2)
        );

        // When converting to domain
        List<BillItem> items = BillMapper.toDomainItems(dtos);

        // Then all should be converted
        assertThat(items).hasSize(2);
        assertThat(items.get(0).getName()).isEqualTo("Laptop");
        assertThat(items.get(1).getName()).isEqualTo("Mouse");
    }

    @Test
    @DisplayName("Should handle category case-insensitively")
    void shouldHandleCategoryCaseInsensitively() {
        // Given DTO with lowercase category
        BillItemDto dto = BillItemDto.of("Milk", "grocery", 5.00, 1);

        // When converting
        BillItem item = BillMapper.toDomainItem(dto);

        // Then should convert correctly (case-insensitive)
        assertThat(item.getCategory()).isEqualTo(ItemCategory.GROCERY);
    }

    @Test
    @DisplayName("Should handle category with extra whitespace")
    void shouldHandleCategoryWithExtraWhitespace() {
        // Given DTO with whitespace around category
        BillItemDto dto = BillItemDto.of("Shirt", "  CLOTHING  ", 50.00, 1);

        // When converting
        BillItem item = BillMapper.toDomainItem(dto);

        // Then whitespace should be trimmed
        assertThat(item.getCategory()).isEqualTo(ItemCategory.CLOTHING);
    }

    @Test
    @DisplayName("Should reject invalid category with helpful error message")
    void shouldRejectInvalidCategoryWithHelpfulErrorMessage() {
        // Given DTO with invalid category
        BillItemDto dto = BillItemDto.of("Item", "INVALID_CATEGORY", 100.00, 1);

        // When converting
        // Then should throw InvalidBillException with helpful message
        assertThatThrownBy(() -> BillMapper.toDomainItem(dto))
                .isInstanceOf(InvalidBillException.class)
                .hasMessageContaining("Invalid category 'INVALID_CATEGORY'")
                .hasMessageContaining("GROCERY")
                .hasMessageContaining("ELECTRONICS")
                .hasMessageContaining("CLOTHING");
    }

    @Test
    @DisplayName("Should reject null category")
    void shouldRejectNullCategory() {
        // Given DTO with null category
        BillItemDto dto = BillItemDto.of("Item", null, 100.00, 1);

        // When converting
        // Then should throw InvalidBillException
        assertThatThrownBy(() -> BillMapper.toDomainItem(dto))
                .isInstanceOf(InvalidBillException.class)
                .hasMessageContaining("category cannot be empty");
    }

    @Test
    @DisplayName("Should reject empty category")
    void shouldRejectEmptyCategory() {
        // Given DTO with empty category
        BillItemDto dto = BillItemDto.of("Item", "   ", 100.00, 1);

        // When converting
        // Then should throw InvalidBillException
        assertThatThrownBy(() -> BillMapper.toDomainItem(dto))
                .isInstanceOf(InvalidBillException.class)
                .hasMessageContaining("category cannot be empty");
    }

    @Test
    @DisplayName("Should wrap domain validation errors in InvalidBillException")
    void shouldWrapDomainValidationErrorsInInvalidBillException() {
        // Given DTO with zero quantity (invalid in domain)
        BillItemDto dto = BillItemDto.of("Item", "ELECTRONICS", 100.00, 0);

        // When converting
        // Then domain validation error should be wrapped
        assertThatThrownBy(() -> BillMapper.toDomainItem(dto))
                .isInstanceOf(InvalidBillException.class)
                .hasMessageContaining("Invalid bill item 'Item'");
    }

    @Test
    @DisplayName("Should handle all valid categories correctly")
    void shouldHandleAllValidCategoriesCorrectly() {
        // Given DTOs for each valid category
        BillItemDto grocery = BillItemDto.of("Milk", "GROCERY", 5.00, 1);
        BillItemDto electronics = BillItemDto.of("Laptop", "ELECTRONICS", 1000.00, 1);
        BillItemDto clothing = BillItemDto.of("Shirt", "CLOTHING", 50.00, 1);
        BillItemDto homeGoods = BillItemDto.of("Lamp", "HOME_GOODS", 75.00, 1);
        BillItemDto other = BillItemDto.of("Book", "OTHER", 20.00, 1);

        // When converting all
        // Then all should succeed
        assertThat(BillMapper.toDomainItem(grocery).getCategory()).isEqualTo(ItemCategory.GROCERY);
        assertThat(BillMapper.toDomainItem(electronics).getCategory()).isEqualTo(ItemCategory.ELECTRONICS);
        assertThat(BillMapper.toDomainItem(clothing).getCategory()).isEqualTo(ItemCategory.CLOTHING);
        assertThat(BillMapper.toDomainItem(homeGoods).getCategory()).isEqualTo(ItemCategory.HOME_GOODS);
        assertThat(BillMapper.toDomainItem(other).getCategory()).isEqualTo(ItemCategory.OTHER);
    }

    @Test
    @DisplayName("Should preserve price precision during conversion")
    void shouldPreservePricePrecisionDuringConversion() {
        // Given DTO with precise decimal price
        BillItemDto dto = BillItemDto.of(
                "Item",
                "ELECTRONICS",
                new BigDecimal("99.99"),
                1
        );

        // When converting
        BillItem item = BillMapper.toDomainItem(dto);

        // Then precision should be preserved
        assertThat(item.getUnitPrice().getAmount())
                .isEqualByComparingTo(new BigDecimal("99.99"));
    }

    @Test
    @DisplayName("Should handle quantity correctly")
    void shouldHandleQuantityCorrectly() {
        // Given DTO with large quantity
        BillItemDto dto = BillItemDto.of("Item", "ELECTRONICS", 10.00, 100);

        // When converting
        BillItem item = BillMapper.toDomainItem(dto);

        // Then quantity should be preserved
        assertThat(item.getQuantity()).isEqualTo(100);

        // And total price should be calculated correctly
        assertThat(item.getTotalPrice()).isEqualTo(Money.of(1000.00));
    }

    @Test
    @DisplayName("Should create informative error messages for invalid items")
    void shouldCreateInformativeErrorMessagesForInvalidItems() {
        // Given DTO with multiple issues
        BillItemDto dto = BillItemDto.of("Problem Item", "INVALID", -10.00, 1);

        // When converting
        // Then error should mention the item name
        assertThatThrownBy(() -> BillMapper.toDomainItem(dto))
                .isInstanceOf(InvalidBillException.class)
                .hasMessageContaining("Problem Item");
    }
}