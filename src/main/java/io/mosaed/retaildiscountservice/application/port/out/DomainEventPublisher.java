package io.mosaed.retaildiscountservice.application.port.out;

/**
 *
 * @author MOSAED ALOTAIBI
 */

import io.mosaed.retaildiscountservice.domain.event.DomainEvent;

/**
 * Port for publishing domain events.
 * This allows the domain to remain decoupled from the event infrastructure.
 */
public interface DomainEventPublisher {

    /**
     * Publish a single domain event
     */
    void publish(DomainEvent event);

    /**
     * Publish multiple domain events
     */
    void publishAll(Iterable<DomainEvent> events);
}
