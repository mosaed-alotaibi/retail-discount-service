package io.mosaed.retaildiscountservice.infrastructure.adapter.rest;

/**
 *
 * @author MOSAED ALOTAIBI
 */

import io.mosaed.retaildiscountservice.application.dto.BillCalculationResponse;
import io.mosaed.retaildiscountservice.application.dto.CalculateBillCommand;
import io.mosaed.retaildiscountservice.application.dto.BillItemDto;
import io.mosaed.retaildiscountservice.application.port.in.CalculateBillUseCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bills")
public class BillController {

    private static final Logger logger = LoggerFactory.getLogger(BillController.class);
    private final CalculateBillUseCase calculateBillUseCase;

    public BillController(CalculateBillUseCase calculateBillUseCase) {
        this.calculateBillUseCase = calculateBillUseCase;
    }

    @PostMapping("/calculate/my-bill")
    public ResponseEntity<BillCalculationResponse> calculateMyBill(
            @Valid @RequestBody BillCalculationRequest request) {

        // Extract the authenticated customer ID from Spring Security context
        String customerId = getAuthenticatedCustomerId();

        logger.info("Calculating bill for authenticated customer: {}", customerId);

        // Create the command using the authenticated customer ID
        CalculateBillCommand command = CalculateBillCommand.of(customerId, request.getItems());

        // Execute the use case
        BillCalculationResponse response = calculateBillUseCase.execute(command);

        logger.info("Bill calculation completed. Bill ID: {}, Net Payable: {}",
                response.getBillId(), response.getNetPayable());

        return ResponseEntity.ok(response);
    }

    private String getAuthenticatedCustomerId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found");
        }

        // The username is the customer ID in our system
        return authentication.getName();
    }

    public static class BillCalculationRequest {
        @Valid
        @NotEmpty(message = "Items list cannot be empty")
        private List<BillItemDto> items;

        public BillCalculationRequest() {}

        public List<BillItemDto> getItems() {
            return items;
        }

        public void setItems(List<BillItemDto> items) {
            this.items = items;
        }
    }

    @PostMapping("/calculate")
    public ResponseEntity<BillCalculationResponse> calculateBill(
            @Valid @RequestBody CalculateBillCommand command) {

        logger.info("Received bill calculation request for customer: {}",
                command.getCustomerId());

        // Invoke the use case through its interface
        // All business logic happens here, hidden behind the interface
        BillCalculationResponse response = calculateBillUseCase.execute(command);

        logger.info("Bill calculation completed. Bill ID: {}, Net Payable: {}",
                response.getBillId(), response.getNetPayable());

        // Return 200 OK with the response in the body
        // Spring automatically serializes the response to JSON
        return ResponseEntity.ok(response);
    }

    /**
     * Health check endpoint to verify the service is running.
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Retail Discount Service is running");
    }
}