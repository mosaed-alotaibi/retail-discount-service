package io.mosaed.retaildiscountservice.application.port.out;

/**
 *
 * @author MOSAED ALOTAIBI
 */

import io.mosaed.retaildiscountservice.domain.model.Bill;
import java.util.Optional;

public interface BillRepository {

    Bill save(Bill bill);

    Optional<Bill> findById(String billId);

    boolean deleteById(String billId);

    boolean existsById(String billId);
}
