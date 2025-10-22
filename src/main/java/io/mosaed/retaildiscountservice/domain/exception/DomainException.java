package io.mosaed.retaildiscountservice.domain.exception;

/**
 *
 * @author MOSAED ALOTAIBI
 */

public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }

    protected DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
