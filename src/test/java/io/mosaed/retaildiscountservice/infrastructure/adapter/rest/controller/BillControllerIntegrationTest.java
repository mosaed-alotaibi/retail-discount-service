package io.mosaed.retaildiscountservice.infrastructure.adapter.rest.controller;

/**
 *
 * @author MOSAED ALOTAIBI
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosaed.retaildiscountservice.application.port.out.BillRepository;
import io.mosaed.retaildiscountservice.application.port.out.CustomerRepository;
import io.mosaed.retaildiscountservice.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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
 * Tests the new RESTful API endpoints.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("BillController API Integration Tests (RESTful)")
class BillControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private BillRepository billRepository;

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

    // ======================================
    // POST /bills - Create Bill Tests
    // ======================================

    @Test
    @DisplayName("POST /bills - Should create bill with 201 Created and Location header")
    void shouldCreateBillWith201AndLocationHeader() throws Exception {
        // Given a valid bill creation request
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

        // When creating bill
        MvcResult result = mockMvc.perform(post("/bills")
                        .with(httpBasic("EMP001", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))

                // Then should return 201 Created with Location header
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.billId").exists())
                .andExpect(jsonPath("$.customerId").value("EMP001"))
                .andExpect(jsonPath("$.totalAmount").value(1000.00))
                .andExpect(jsonPath("$.percentageDiscountRate").value(30))
                .andExpect(jsonPath("$.percentageDiscount").value(300.00))
                .andExpect(jsonPath("$.netPayable").value(665.00))
                .andReturn();

        // Verify Location header format
        String location = result.getResponse().getHeader("Location");
        assertThat(location).contains("/bills/");
    }

    @Test
    @DisplayName("POST /bills - Should create bill using authenticated user when customerId omitted")
    void shouldCreateBillUsingAuthenticatedUser() throws Exception {
        // Given request WITHOUT customerId (should use authenticated user)
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

        // When creating bill
        mockMvc.perform(post("/bills")
                        .with(httpBasic("EMP001", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                // Then should succeed using customer from authentication
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerId").value("EMP001"))
                .andExpect(jsonPath("$.percentageDiscountRate").value(30));
    }

    @Test
    @DisplayName("POST /bills - Should reject unauthenticated request")
    void shouldRejectUnauthenticatedRequest() throws Exception {
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

        // When making request without auth
        mockMvc.perform(post("/bills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /bills - Should return 404 when customer not found")
    void shouldReturn404WhenCustomerNotFound() throws Exception {
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

        mockMvc.perform(post("/bills")
                        .with(httpBasic("EMP001", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Customer not found with ID: NOTEXIST"));
    }

    @Test
    @DisplayName("POST /bills - Should return 400 for validation errors")
    void shouldReturn400ForValidationErrors() throws Exception {
        Map<String, Object> request = Map.of(
                "customerId", "EMP001",
                "items", List.of()  // Empty list - invalid
        );

        mockMvc.perform(post("/bills")
                        .with(httpBasic("EMP001", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("POST /bills - Should handle multiple items correctly")
    void shouldHandleMultipleItemsCorrectly() throws Exception {
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

        mockMvc.perform(post("/bills")
                        .with(httpBasic("EMP001", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalAmount").value(1115.00))
                .andExpect(jsonPath("$.percentageDiscount").value(330.00))
                .andExpect(jsonPath("$.billBasedDiscount").value(35.00))
                .andExpect(jsonPath("$.netPayable").value(750.00));
    }

    // ======================================
    // GET /bills/{id} - Retrieve Bill Tests
    // ======================================

    @Test
    @DisplayName("GET /bills/{id} - Should retrieve bill by ID")
    void shouldRetrieveBillById() throws Exception {
        // Given a bill exists
        Customer customer = customerRepository.findById("EMP001").orElseThrow();
        Bill bill = Bill.create(
                customer,
                List.of(BillItem.of("Laptop", ItemCategory.ELECTRONICS, Money.of(1000.00), 1))
        );
        bill.calculateDiscount();
        Bill savedBill = billRepository.save(bill);

        // When retrieving the bill
        mockMvc.perform(get("/bills/" + savedBill.getBillId())
                        .with(httpBasic("EMP001", "password")))

                // Then should return the bill
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.billId").value(savedBill.getBillId()))
                .andExpect(jsonPath("$.customerId").value("EMP001"))
                .andExpect(jsonPath("$.totalAmount").value(1000.00));
    }

    @Test
    @DisplayName("GET /bills/{id} - Should return 404 when bill not found")
    void shouldReturn404WhenBillNotFound() throws Exception {
        mockMvc.perform(get("/bills/NONEXISTENT")
                        .with(httpBasic("EMP001", "password")))
                .andExpect(status().isNotFound());
    }

    // ======================================
    // GET /bills - List Bills Tests
    // ======================================

    @Test
    @DisplayName("GET /bills?customerId - Should list bills for customer")
    void shouldListBillsForCustomer() throws Exception {
        // Given customer has bills
        Customer customer = customerRepository.findById("EMP001").orElseThrow();
        Bill bill1 = Bill.create(customer, List.of(BillItem.of("Item1", ItemCategory.ELECTRONICS, Money.of(100.00), 1)));
        Bill bill2 = Bill.create(customer, List.of(BillItem.of("Item2", ItemCategory.ELECTRONICS, Money.of(200.00), 1)));
        bill1.calculateDiscount();
        bill2.calculateDiscount();
        billRepository.save(bill1);
        billRepository.save(bill2);

        // When listing bills
        mockMvc.perform(get("/bills")
                        .param("customerId", "EMP001")
                        .with(httpBasic("EMP001", "password")))

                // Then should return customer bills
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].customerId").value("EMP001"))
                .andExpect(jsonPath("$[1].customerId").value("EMP001"));
    }

    @Test
    @DisplayName("GET /bills - Should list bills using authenticated user when customerId omitted")
    void shouldListBillsUsingAuthenticatedUser() throws Exception {
        // Given customer has bills
        Customer customer = customerRepository.findById("CUST001").orElseThrow();
        Bill bill = Bill.create(customer, List.of(BillItem.of("Item", ItemCategory.ELECTRONICS, Money.of(100.00), 1)));
        bill.calculateDiscount();
        billRepository.save(bill);

        // When listing bills without customerId parameter
        mockMvc.perform(get("/bills")
                        .with(httpBasic("CUST001", "password")))

                // Then should return bills for authenticated user
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].customerId").value("CUST001"));
    }

    // ======================================
    // Business Logic Tests
    // ======================================

    @Test
    @DisplayName("Should apply correct discount for regular customer")
    void shouldApplyCorrectDiscountForRegularCustomer() throws Exception {
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

        mockMvc.perform(post("/bills")
                        .with(httpBasic("CUST001", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                // Regular customer (< 2 years) gets no percentage discount
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.percentageDiscountRate").value(0))
                .andExpect(jsonPath("$.percentageDiscount").value(0.00))
                .andExpect(jsonPath("$.billBasedDiscount").value(50.00))
                .andExpect(jsonPath("$.netPayable").value(950.00));
    }
}
