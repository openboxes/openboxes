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
 * Represents a particular Event of interest during the course of a Shipment
 * Examples might be:
 *
 *  Shipment #1 Departed from Boston on 1/1/2010:
 *{eventDate: 1/1/2010, eventLocation: Boston, eventType: SHIPPED}*
 *  Shipment #2 Arrived at Customs on 5/5/2010:
 *{eventDate: 5/5/2010, eventLocation: Customs, eventType: ARRIVED}*/
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
