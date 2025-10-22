package io.mosaed.retaildiscountservice.domain.exception;

/**
 *
 * @author MOSAED ALOTAIBI
 */

public class InvalidBillException extends DomainException {

    public InvalidBillException(String message) {
        super(message);
    }

    public InvalidBillException(String message, Throwable cause) {
        super(message, cause);
    }
}
