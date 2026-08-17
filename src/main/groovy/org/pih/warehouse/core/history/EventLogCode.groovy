package org.pih.warehouse.core.history

/**
 * Enumerates the types of logs as used in {@link EventLog}.
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
    EVENT_ROLLBACK_OCCURRED,

    /**
     * Logs an error occurrence while automatically processing an object. The message holds the error
     * text; event is always null since there is no corresponding {@link org.pih.warehouse.core.Event} for a failure.
     *
     * FIXME: this is a pragmatic reuse of this generic code for error auditing. Whether a given entry is an
     *  allocation or issuance failure is not stored explicitly - it's inferred from the message in event log.
     *  The proper solution is a dedicated classification subtype on EventLog.
     */
    ERROR_OCCURRED,
}
