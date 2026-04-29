package org.pih.warehouse.core.event

import org.pih.warehouse.core.Event
import org.pih.warehouse.core.EventCode
import org.pih.warehouse.core.EventType

/**
 * Manages the lifecycle (creating and rolling back) of an {@link Event}.
 */
abstract class EventManager {

    /**
     * Fetches the event type associated with the given code, creating one if it does not exist yet.
     *
     * Not to be used for the CUSTOM event code since there can be more than one custom event type.
     */
    EventType getOrCreateEventType(EventCode eventCode) {
        if (eventCode == EventCode.CUSTOM) {
            throw new RuntimeException("Event code is not a unique identifier for custom events. Fetch by id instead.")
        }

        // Non-custom event types are expected to have a one-to-one mapping to event code
        EventType eventType = EventType.findByEventCode(eventCode)
        if (eventType) {
            return eventType
        }

        /*
         * We create the event type if one does not exist for the given code purely for the sake of convenience.
         * It saves us from needing to also create an event type via migration when we add a new event code to
         * the system. This is only safe to do because non-custom event types have a one-to-one mapping to code.
         * Implementations will still be able to disable these event types by setting supported to false.
         */
        return new EventType(
                name: eventCode.toString(),
                eventCode: eventCode,
        ).save(failOnError: true)
    }
}
