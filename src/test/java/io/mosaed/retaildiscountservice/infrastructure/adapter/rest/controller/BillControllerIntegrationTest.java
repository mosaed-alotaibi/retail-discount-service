package io.mosaed.retaildiscountservice.infrastructure.adapter.rest.controller;

/**
 *
 * @author MOSAED ALOTAIBI
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosaed.retaildiscountservice.application.dto.BillItemDto;
import io.mosaed.retaildiscountservice.application.port.out.CustomerRepository;
import io.mosaed.retaildiscountservice.domain.model.Customer;
import io.mosaed.retaildiscountservice.domain.model.CustomerType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for BillController with full Spring context.
 *
 * TESTING STRATEGY:
 *
 * These are "end-to-end" API tests that verify the complete stack:
 * - HTTP request/response handling
 * - JSON serialization/deserialization
 * - Spring Security authentication
 * - Request validation
 * - Exception handling and HTTP status codes
 * - Integration of all layers working together
 *
 * WHY @SpringBootTest?
 *
 * This annotation loads the FULL Spring application context, including:
 * - All controllers
 * - All services
 * - All repositories
 * - Security configuration
 * - Everything configured in the application
 *
 * WHY @AutoConfigureMockMvc?
 *
 * This configures MockMvc, which allows us to make HTTP requests to our
 * controllers without starting a real HTTP server. It's faster than a
 * real server but tests the full web layer.
 *
 * TESTING WITH SECURITY:
 *
 * We test both authenticated and unauthenticated scenarios:
 * - httpBasic() - Provides HTTP Basic authentication credentials
 * - @WithMockUser - Mocks a logged-in user for simpler tests
 * - No auth - Tests that endpoints are properly secured
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("BillController API Integration Tests")
class BillControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerRepository customerRepository;

    @BeforeEach
    void setUp() {
        // Create test customers for authentication
        customerRepository.save(Customer.of(
                "EMP001",
                CustomerType.EMPLOYEE,
                LocalDate.now().minusYears(1)
        ));

        customerRepository.save(Customer.of(
                "CUST001",
                CustomerType.REGULAR,
                LocalDate.now().minusMonths(6)
        ));
    }

    @Test
    @DisplayName("Should calculate bill with valid authentication")
    void shouldCalculateBillWithValidAuthentication() throws Exception {
        // Given a valid bill calculation request
        Map<String, Object> request = Map.of(
                "customerId", "EMP001",
                "items", List.of(
                        Map.of(
                                "name", "Laptop",
                                "category", "ELECTRONICS",
                                "unitPrice", 1000.00,
                                "quantity", 1
                        )
                )
        );

        String requestJson = objectMapper.writeValueAsString(request);

        // When making authenticated request
        MvcResult result = mockMvc.perform(post("/bills/calculate")
                        .with(httpBasic("EMP001", "password"))  // HTTP Basic authentication
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))

                // Then should return 200 OK
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.billId").exists())
                .andExpect(jsonPath("$.customerId").value("EMP001"))
                .andExpect(jsonPath("$.totalAmount").value(1000.00))
                .andExpect(jsonPath("$.percentageDiscountRate").value(30))
                .andExpect(jsonPath("$.percentageDiscount").value(300.00))
                .andExpect(jsonPath("$.netPayable").value(665.00))
                .andReturn();

        // Verify response structure
        String responseJson = result.getResponse().getContentAsString();
        assertThat(responseJson).contains("billId");
        assertThat(responseJson).contains("netPayable");
    }

    @Test
    @DisplayName("Should reject unauthenticated request")
    void shouldRejectUnauthenticatedRequest() throws Exception {
        // Given a request without authentication
        Map<String, Object> request = Map.of(
                "customerId", "EMP001",
                "items", List.of(
                        Map.of(
                                "name", "Laptop",
                                "category", "ELECTRONICS",
                                "unitPrice", 1000.00,
                                "quantity", 1
                        )
                )
        );

        String requestJson = objectMapper.writeValueAsString(request);

        // When making request without auth
        // Then should return 401 Unauthorized
        mockMvc.perform(post("/bills/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should reject invalid credentials")
    void shouldRejectInvalidCredentials() throws Exception {
        // Given request with wrong password
        Map<String, Object> request = Map.of(
                "customerId", "EMP001",
                "items", List.of(
                        Map.of(
                                "name", "Laptop",
                                "category", "ELECTRONICS",
                                "unitPrice", 1000.00,
                                "quantity", 1
                        )
                )
        );

        // When authenticating with wrong password
        // Then should return 401 Unauthorized
        mockMvc.perform(post("/bills/calculate")
                        .with(httpBasic("EMP001", "wrongpassword"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 404 when customer not found")
    void shouldReturn404WhenCustomerNotFound() throws Exception {
        // Given request with non-existent customer
        Map<String, Object> request = Map.of(
                "customerId", "NOTEXIST",
                "items", List.of(
                        Map.of(
                                "name", "Laptop",
                                "category", "ELECTRONICS",
                                "unitPrice", 1000.00,
                                "quantity", 1
                        )
                )
        );

        // When making request
        // Then should return 404 Not Found
        mockMvc.perform(post("/bills/calculate")
                        .with(httpBasic("EMP001", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Customer not found with ID: NOTEXIST"));
    }

    @Test
    @DisplayName("Should return 400 for validation errors")
    void shouldReturn400ForValidationErrors() throws Exception {
        // Given request with invalid data (empty items list)
        Map<String, Object> request = Map.of(
                "customerId", "EMP001",
                "items", List.of()  // Empty list - invalid
        );

        // When making request
        // Then should return 400 Bad Request
        mockMvc.perform(post("/bills/calculate")
                        .with(httpBasic("EMP001", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should return 400 for invalid item category")
    void shouldReturn400ForInvalidItemCategory() throws Exception {
        // Given request with invalid category
        Map<String, Object> request = Map.of(
                "customerId", "EMP001",
                "items", List.of(
                        Map.of(
                                "name", "Laptop",
                                "category", "INVALID_CATEGORY",  // Invalid!
                                "unitPrice", 1000.00,
                                "quantity", 1
                        )
                )
        );

        // When making request
        // Then should return 400 Bad Request with helpful message
        mockMvc.perform(post("/bills/calculate")
                        .with(httpBasic("EMP001", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("Invalid category")
                ));
    }

    @Test
    @DisplayName("Should handle authenticated endpoint with customer ID from auth context")
    void shouldHandleAuthenticatedEndpointWithCustomerIdFromAuthContext() throws Exception {
        // Given request WITHOUT customerId (comes from authentication)
        Map<String, Object> request = Map.of(
                "items", List.of(
                        Map.of(
                                "name", "Laptop",
                                "category", "ELECTRONICS",
                                "unitPrice", 1000.00,
                                "quantity", 1
                        )
                )
        );

        // When making authenticated request to /my-bill endpoint
        mockMvc.perform(post("/bills/calculate/my-bill")
                        .with(httpBasic("EMP001", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                // Then should succeed using customer from authentication
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value("EMP001"))
                .andExpect(jsonPath("$.percentageDiscountRate").value(30));
    }

    @Test
    @DisplayName("Should allow health check without authentication")
    void shouldAllowHealthCheckWithoutAuthentication() throws Exception {
        // When accessing health endpoint without auth
        // Then should succeed (health check is public)
        mockMvc.perform(get("/bills/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Retail Discount Service is running"));
    }

    @Test
    @DisplayName("Should handle multiple items in bill correctly")
    void shouldHandleMultipleItemsInBillCorrectly() throws Exception {
        // Given request with multiple mixed items
        Map<String, Object> request = Map.of(
                "customerId", "EMP001",
                "items", List.of(
                        Map.of(
                                "name", "Laptop",
                                "category", "ELECTRONICS",
                                "unitPrice", 1000.00,
                                "quantity", 1
                        ),
                        Map.of(
                                "name", "Mouse",
                                "category", "ELECTRONICS",
                                "unitPrice", 50.00,
                                "quantity", 2
                        ),
                        Map.of(
                                "name", "Milk",
                                "category", "GROCERY",
                                "unitPrice", 5.00,
                                "quantity", 3
                        )
                )
        );

        // When making request
        mockMvc.perform(post("/bills/calculate")
                        .with(httpBasic("EMP001", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                // Then calculation should be correct
                // Total: $1115, Percentage: $330, Bill-based: $35, Net: $750
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAmount").value(1115.00))
                .andExpect(jsonPath("$.percentageDiscount").value(330.00))
                .andExpect(jsonPath("$.billBasedDiscount").value(35.00))
                .andExpect(jsonPath("$.netPayable").value(750.00));
    }

    @Test
    @DisplayName("Should apply correct discount for regular customer")
    void shouldApplyCorrectDiscountForRegularCustomer() throws Exception {
        // Given regular customer request
        Map<String, Object> request = Map.of(
                "customerId", "CUST001",
                "items", List.of(
                        Map.of(
                                "name", "Laptop",
                                "category", "ELECTRONICS",
                                "unitPrice", 1000.00,
                                "quantity", 1
                        )
                )
        );

        // When making request
        mockMvc.perform(post("/bills/calculate")
                        .with(httpBasic("CUST001", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                // Then should get NO percentage discount (too new)
                // Only bill-based: $50
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.percentageDiscountRate").value(0))
                .andExpect(jsonPath("$.percentageDiscount").value(0.00))
                .andExpect(jsonPath("$.billBasedDiscount").value(50.00))
                .andExpect(jsonPath("$.netPayable").value(950.00));
    }
}