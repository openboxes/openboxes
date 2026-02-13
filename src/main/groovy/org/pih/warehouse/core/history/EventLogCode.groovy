package org.pih.warehouse.core.history

/**
 * Enumerates the types logs as used in {@link EventLog}.
 *
 * EventLogCodes should be broad, categorical, and general purpose. Custom, feature-specific types of events should be
 * created as an {@link org.pih.warehouse.core.EventType} so that the system remains flexible to a dynamic range of
 * use cases.
 */
enum EventLogCode {

    /**
     * Logs the occurrence of an {@link org.pih.warehouse.core.Event}
     */
    EVENT_OCCURRED,

    /**
     * Logs the occurrence of an {@link org.pih.warehouse.core.Event} being rolled back
     */
    ROLLBACK_EVENT_OCCURRED,
}
