package org.pih.warehouse.core.history

import java.time.Instant

import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.Event
import org.pih.warehouse.core.EventCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User

/**
 * An audit log representing some action that occurred on some entity or system.
 *
 * The event log is feature-agnostic, and so it is left to the individual features to determine what specific actions
 * are worthy of an entry in this table.
 *
 * Generally, EventLogs are meant to act as an audit log for admin purposes only. As such, they should not be used
 * as a part of any internal logic. If you need the ability to track state changes within some system, use
 * {@link Event}.
 *
 * A typical usage of EventLog is in conjunction with a {@link HistoryBuilder}, using the EventLog to construct
 * the results of the getHistory() method. This standardizes process of operating on and rendering audit logs in
 * the system.
 */
class EventLog implements Comparable<EventLog>, Serializable {

    def beforeInsert() {
        createdBy = AuthService.currentUser
        updatedBy = AuthService.currentUser
    }

    def beforeUpdate() {
        updatedBy = AuthService.currentUser
    }

    String id

    /**
     * The Event associated with the log. We typically want to create an EventLog whenever an Event is created,
     * updated, or rolled back. This field will be null for non-event-related actions and for rolled back Events
     * (because the Event is deleted).
     */
    Event event

    /**
     * Events can be deleted (in the case of rollbacks). Storing the EventCode independently from the Event
     * allows us to be able to know the action that was taken, even when the Event itself no longer exists.
     */
    EventCode eventCode

    /**
     * Events can be deleted (in the case of rollbacks). Storing the event data independently from the Event
     * allows us to be able to know when the action was taken, even when the Event itself no longer exists.
     */
    Instant eventDate

    /**
     * The type of event log.
     */
    EventLogCode eventLogCode

    /**
     * A (non-localized) message describing the specifics of the actual change being performed.
     * For example: "Recipient updated from Alice to Bob"
     */
    String message

    /**
     * The location in which the action took place. Typically this will be the facility that is actively involved
     * in the action.
     */
    Location location

    // Audit fields
    Instant dateCreated
    Instant lastUpdated
    User createdBy
    User updatedBy

    static constraints = {
        id generator: "uuid"
        event(nullable: true)
        eventCode(nullable: true)
        message(nullable: true)
        location(nullable: true)
        createdBy(nullable: true)
        updatedBy(nullable: true)
    }

    String toString() {
        return "${eventLogCode}: ${message}"
    }

    int compareTo(EventLog other) {
        return eventDate <=> other?.eventDate ?:
               dateCreated <=> other?.dateCreated ?:
               lastUpdated <=> other?.lastUpdated
    }

    Map toJson() {
        return [
                id: id,
                event: event.toJson(),
                eventLogCode: eventLogCode,
                message: message,
                location: locationId,
                dateCreated: dateCreated,
                lastUpdated: lastUpdated,
                createdBy: createdBy?.name,
                updatedBy: updatedBy?.name,
        ]
    }
}
