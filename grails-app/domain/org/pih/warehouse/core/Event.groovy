/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.core

/**
 * Represents a state-altering Event on some entity or system.
 *
 * The Event table is feature-agnostic, and so it is left to the individual features to determine what specific events
 * (as enumerated in EventType) are worthy of an entry in this table.
 *
 * Generally, Events are meant to track changes to the state of a system (ex: receiving a shipment) and are NOT meant
 * to be a full audit/event log (ex: changing the recipient of a shipment will not create an Event). If you need a
 * full audit log, use Event in combination with {@link EventLog}.
 *
 * Additionally, we only store a single Event per EventType/state change. For example, if a receipt rollback occurs,
 * the Event associated with that receipt will be deleted. As such, the Event table can be viewed as a means to document
 * the "active" state changes of some system. This makes it a useful tool for standardizing and simplifying how we store
 * and calculate system state, but should NOT be used to determine historical state changes, especially when state is
 * non-linear (ie if we allow state to be rolled back).
 *
 * Example Events for a Shipment might be:
 * - Shipment order is created on Jan 1    -> [eventDate: 2025-01-01, eventLocation: Boston, eventType: CREATED]
 * - Shipment departs from Boston on Jan 2 -> [eventDate: 2025-01-02, eventLocation: Boston, eventType: SHIPPED]
 * - Shipment arrives at X Depot on Jan 20 -> [eventDate: 2025-01-20, eventLocation: X Depot, eventType: RECEIVED]
 */
class Event implements Comparable, Serializable, Historizable {

    String id
    Date eventDate                // The date and time on which the Event occurred
    EventType eventType            // The type of the Event
    Location eventLocation        // The Location at which the Event occurred
    Date dateCreated
    Date lastUpdated
    User createdBy
    Comment comment

    static mapping = {
        id generator: 'uuid'
    }


    static constraints = {
        eventDate(nullable: true)
        eventType(nullable: true)
        eventLocation(nullable: true)
        createdBy(nullable: true)
        comment(nullable: true)
    }

    @Override
    ReferenceDocument getReferenceDocument() {
        return new ReferenceDocument(
                label: eventType?.description,
                id: id,
                identifier: id,
        )
    }

    @Override
    List<HistoryItem<Event>> getHistory() {
        HistoryItem<Event> historyItem = new HistoryItem<>(
                date: eventDate,
                location: eventLocation,
                eventType: eventType?.toDto(),
                comment: comment,
                createdBy: createdBy,
                referenceDocument: getReferenceDocument()
        )
       return [historyItem]
    }

    String toString() { return "$eventType $eventLocation on $eventDate" }

    int compareTo(obj) {
        def diff = obj?.eventDate <=> eventDate
        if (diff == 0) {
            diff = obj?.eventType <=> eventType
        }
        if (diff == 0) {
            diff = obj?.dateCreated <=> dateCreated
        }
        return diff
    }

    Map toJson() {
        return [
                id: id,
                eventDate: eventDate,
                eventType: [
                        name: eventType?.name,
                        eventCode: eventType?.eventCode?.name(),
                ],
                createdBy: createdBy?.name,
                comment: comment?.comment
        ]
    }
}
