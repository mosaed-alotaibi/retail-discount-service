package io.mosaed.retaildiscountservice.infrastructure.adapter.rest;

/**
 *
 * @author MOSAED ALOTAIBI
 */

import io.mosaed.retaildiscountservice.application.dto.BillCalculationResponse;
import io.mosaed.retaildiscountservice.application.dto.CalculateBillCommand;
import io.mosaed.retaildiscountservice.application.dto.BillItemDto;
import io.mosaed.retaildiscountservice.application.port.in.CalculateBillUseCase;
import io.mosaed.retaildiscountservice.application.port.in.GetBillUseCase;
import io.mosaed.retaildiscountservice.application.port.in.ListBillsUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

/**
 * REST controller for Bill resource.
 * Follows RESTful principles with resource-oriented URLs.
 */
@RestController
@RequestMapping("/bills")
@Tag(name = "Bills", description = "Bill management API - Create and retrieve retail bills with calculated discounts")
@SecurityRequirement(name = "HTTP Basic Auth")
public class BillController {

    private static final Logger logger = LoggerFactory.getLogger(BillController.class);

    private final CalculateBillUseCase calculateBillUseCase;
    private final GetBillUseCase getBillUseCase;
    private final ListBillsUseCase listBillsUseCase;

    public BillController(
            CalculateBillUseCase calculateBillUseCase,
            GetBillUseCase getBillUseCase,
            ListBillsUseCase listBillsUseCase) {
        this.calculateBillUseCase = calculateBillUseCase;
        this.getBillUseCase = getBillUseCase;
        this.listBillsUseCase = listBillsUseCase;
    }

    /**
     * Create a new bill (POST /bills)
     * RESTful: Creating a bill resource
     *
     * This endpoint supports two modes:
     * 1. If customerId is provided in request body, use that customer
     * 2. If customerId is null/empty, use the authenticated user's ID
     */
    @Operation(
            summary = "Create a new bill",
            description = "Creates a new bill and calculates discounts based on customer type. " +
                    "Returns the calculated bill with percentage and bill-based discounts applied."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Bill created successfully",
                    content = @Content(schema = @Schema(implementation = BillCalculationResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body or validation error"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Customer not found"
            )
    })
    @PostMapping
    public ResponseEntity<BillCalculationResponse> createBill(
            @Parameter(description = "Bill creation request with items and optional customer ID")
            @Valid @RequestBody CreateBillRequest request) {

        // Determine customer ID: use request body if provided, otherwise use authenticated user
        String customerId = (request.getCustomerId() != null && !request.getCustomerId().isBlank())
                ? request.getCustomerId()
                : getAuthenticatedCustomerId();

        logger.info("Creating bill for customer: {}", customerId);

        // Create the command
        CalculateBillCommand command = CalculateBillCommand.of(customerId, request.getItems());

        // Execute the use case
        BillCalculationResponse response = calculateBillUseCase.execute(command);

        logger.info("Bill created successfully. Bill ID: {}, Net Payable: {}",
                response.getBillId(), response.getNetPayable());

        // Build Location header: /bills/{billId}
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getBillId())
                .toUri();

        // Return 201 Created with Location header
        return ResponseEntity.created(location).body(response);
    }

    /**
     * Get a bill by ID (GET /bills/{id})
     * RESTful: Retrieving a specific bill resource
     */
    @Operation(
            summary = "Get a bill by ID",
            description = "Retrieves a specific bill by its unique identifier"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Bill found and returned",
                    content = @Content(schema = @Schema(implementation = BillCalculationResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Bill not found"
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<BillCalculationResponse> getBill(
            @Parameter(description = "Unique identifier of the bill", required = true)
            @PathVariable String id) {
        logger.info("Retrieving bill: {}", id);

        return getBillUseCase.execute(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * List bills with optional filters (GET /bills?customerId=X&from=Y&to=Z)
     * RESTful: Querying bill resources
     *
     * Query parameters:
     * - customerId: Filter by customer (required if not authenticated)
     * - from: Start date for filtering (optional, requires 'to')
     * - to: End date for filtering (optional, requires 'from')
     * - limit: Limit results (for recent bills when no customerId provided)
     */
    @Operation(
            summary = "List bills with filters",
            description = "Query bills with optional filters: customer ID, date range, or recent bills limit"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "List of bills matching criteria",
                    content = @Content(schema = @Schema(implementation = BillCalculationResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            )
    })
    @GetMapping
    public ResponseEntity<List<BillCalculationResponse>> listBills(
            @Parameter(description = "Filter by customer ID (uses authenticated user if not provided)")
            @RequestParam(required = false) String customerId,
            @Parameter(description = "Start date for date range filter (ISO-8601 format)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @Parameter(description = "End date for date range filter (ISO-8601 format)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @Parameter(description = "Limit number of results (default: 10)")
            @RequestParam(required = false, defaultValue = "10") int limit) {

        // If customerId is not provided, use authenticated user
        String effectiveCustomerId = (customerId != null && !customerId.isBlank())
                ? customerId
                : getAuthenticatedCustomerIdOrNull();

        // Query based on parameters
        List<BillCalculationResponse> bills;

        if (effectiveCustomerId != null) {
            if (from != null && to != null) {
                logger.info("Listing bills for customer {} from {} to {}", effectiveCustomerId, from, to);
                bills = listBillsUseCase.findByCustomerIdAndDateRange(effectiveCustomerId, from, to);
            } else {
                logger.info("Listing all bills for customer {}", effectiveCustomerId);
                bills = listBillsUseCase.findByCustomerId(effectiveCustomerId);
            }
        } else {
            // No customer specified, return recent bills
            logger.info("Listing {} recent bills", limit);
            bills = listBillsUseCase.findRecentBills(limit);
        }

        return ResponseEntity.ok(bills);
    }

    /**
     * Get authenticated customer ID from Spring Security context
     */
    private String getAuthenticatedCustomerId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found");
        }

        return authentication.getName();
    }

    /**
     * Get authenticated customer ID or null if not authenticated
     */
    private String getAuthenticatedCustomerIdOrNull() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                return authentication.getName();
            }
        } catch (Exception e) {
            // Ignore
        }
        return null;
    }

    /**
     * Request DTO for creating a bill
     */
    public static class CreateBillRequest {
        // Optional: if not provided, will use authenticated user's ID
        private String customerId;

        @Valid
        @NotEmpty(message = "Items list cannot be empty")
        private List<BillItemDto> items;

        public CreateBillRequest() {}

        public String getCustomerId() {
            return customerId;
        }

        public void setCustomerId(String customerId) {
            this.customerId = customerId;
        }

        public List<BillItemDto> getItems() {
            return items;
        }

        public void setItems(List<BillItemDto> items) {
            this.items = items;
        }
    }
}