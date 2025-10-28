package io.mosaed.retaildiscountservice.application.port.out;

/**
 *
 * @author MOSAED ALOTAIBI
 */

import io.mosaed.retaildiscountservice.domain.model.Bill;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository port for Bill aggregate.
 * This interface defines domain-focused queries rather than generic CRUD operations.
 */
public interface BillRepository {

    /**
     * Save a bill (create or update)
     */
    Bill save(Bill bill);

    /**
     * Find a bill by its unique identifier
     */
    Optional<Bill> findById(String billId);

    /**
     * Find all bills for a specific customer
     * This supports the domain need to review customer purchase history
     */
    List<Bill> findByCustomerId(String customerId);

    /**
     * Find bills for a customer within a date range
     * This supports domain scenarios like monthly reports or period-based analytics
     */
    List<Bill> findByCustomerIdAndDateRange(String customerId, LocalDateTime from, LocalDateTime to);

    /**
     * Find recent bills (useful for admin/monitoring)
     */
    List<Bill> findRecentBills(int limit);

    /**
     * Delete a bill by ID
     */
    boolean deleteById(String billId);

    /**
     * Check if a bill exists
     */
    boolean existsById(String billId);
}
