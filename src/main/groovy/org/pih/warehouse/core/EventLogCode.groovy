package org.pih.warehouse.core

/**
 * Enumerates the types logs as used in {@link EventLog}.
 *
 * EventLogCodes should be broad, categorical, and general purpose. Custom, feature-specific types of events should be
 * created as an {@Link EventType} so that the system remains flexible to a dynamic range of use cases.
 */
enum EventLogCode {

    /**
     * Logs the occurrence of an {@link Event}
     */
    EVENT_OCCURRED,

    /**
     * Logs the occurrence of an {@link Event} being rolled back
     */
    ROLLBACK_EVENT_OCCURRED,

    /**
     * Denotes fields of some entity being modified from their original values.
     */
    FIELDS_UPDATED,
}
