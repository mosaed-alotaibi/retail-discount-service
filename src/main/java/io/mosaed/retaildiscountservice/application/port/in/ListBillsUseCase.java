package io.mosaed.retaildiscountservice.application.port.in;

/**
 *
 * @author MOSAED ALOTAIBI
 */

import io.mosaed.retaildiscountservice.application.dto.BillCalculationResponse;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Use case for querying bills with various filters.
 * This supports the domain need to review purchase history and generate reports.
 */
public interface ListBillsUseCase {

    /**
     * List all bills for a specific customer
     */
    List<BillCalculationResponse> findByCustomerId(String customerId);

    /**
     * List bills for a customer within a date range
     */
    List<BillCalculationResponse> findByCustomerIdAndDateRange(
            String customerId,
            LocalDateTime from,
            LocalDateTime to
    );

    /**
     * List recent bills (useful for admin/monitoring)
     */
    List<BillCalculationResponse> findRecentBills(int limit);
}
