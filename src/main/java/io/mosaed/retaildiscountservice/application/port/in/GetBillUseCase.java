package io.mosaed.retaildiscountservice.application.port.in;

/**
 *
 * @author MOSAED ALOTAIBI
 */

import io.mosaed.retaildiscountservice.application.dto.BillCalculationResponse;

import java.util.Optional;

/**
 * Use case for retrieving a bill by its ID.
 * This supports the domain need to view bill details after calculation.
 */
public interface GetBillUseCase {

    /**
     * Retrieve a bill by its unique identifier
     * @param billId The unique identifier of the bill
     * @return The bill details if found
     */
    Optional<BillCalculationResponse> execute(String billId);
}
