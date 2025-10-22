package io.mosaed.retaildiscountservice.application.port.in;

/**
 *
 * @author MOSAED ALOTAIBI
 */

import io.mosaed.retaildiscountservice.application.dto.BillCalculationResponse;
import io.mosaed.retaildiscountservice.application.dto.CalculateBillCommand;

public interface CalculateBillUseCase {
    BillCalculationResponse execute(CalculateBillCommand command);
}
