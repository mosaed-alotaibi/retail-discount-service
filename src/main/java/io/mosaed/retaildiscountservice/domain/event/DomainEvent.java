package io.mosaed.retaildiscountservice.domain.event;

/**
 *
 * @author MOSAED ALOTAIBI
 */

import java.time.LocalDateTime;

/**
 * Base interface for all domain events.
 * Domain events represent something that happened in the domain that domain experts care about.
 */
public interface DomainEvent {

    /**
     * When the event occurred
     */
    LocalDateTime occurredOn();

    /**
     * Type of the event
     */
    String eventType();
}
