package org.pih.warehouse.core

import java.time.Instant
import org.apache.commons.lang.StringUtils

import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.date.JavaUtilDateParser

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
 * A typical usage of EventLog is having an entity with a hasMany relationship to EventLog. That entity can implement
 * {@link Historizable}, using the EventLog to construct the results of the getHistory() method. This allows us to
 * standardize the process of operating on and rendering audit logs in the system.
 */
class EventLog implements Comparable<EventLog>, Serializable, Historizable {

    def beforeInsert() {
        createdBy = AuthService.currentUser
        updatedBy = AuthService.currentUser
    }

    def beforeUpdate() {
        updatedBy = AuthService.currentUser
    }

    String id

    /**
     * The Event associated with the log. It is typical that we create an EventLog whenever an Event is created,
     * updated, or rolled back. This field will be null for non-event-related actions.
     */
    Event event

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
        message(nullable: true)
        location(nullable: true)
        createdBy(nullable: true)
        updatedBy(nullable: true)
    }

    String toString() {
        return "${eventLogCode}: ${message}"
    }

    int compareTo(EventLog other) {
        return dateCreated <=> other?.dateCreated ?:
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

    @Override
    List<HistoryItem> getHistory() {
        // Event-based logs
        if (event) {
            List<HistoryItem> history = event.getHistory()
            for (HistoryItem item in history) {
                item.eventType.name = eventLogCode  // TODO: l10n it!
                if (StringUtils.isNotBlank(message)) {
                    item.comment = new Comment(comment: message)
                }
            }
            return history
        }

        // Non-event-based logs (rolled back Events will be a part of this block because the Event is deleted)
        return [new HistoryItem(
                date: JavaUtilDateParser.asDate(dateCreated),
                location: location,
                referenceDocument: getReferenceDocument(),
                eventType: new EventTypeDto(
                        name: eventLogCode,  // TODO: l10n it!
                        eventCode: null,
                ),
                comment: new Comment(comment: message),
                createdBy: createdBy,
        )]
    }

    @Override
    ReferenceDocument getReferenceDocument() {
        // The event logs don't have a back-reference to the document that created them so we cannot
        // build the association here. We expect the feature-specific Historizable entities to build their
        // own reference documents based on their own data.
        return null
    }
}
