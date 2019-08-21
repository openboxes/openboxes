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
class Event implements Comparable, Serializable {

    String id
    Date eventDate                // The date and time on which the Event occurred
    EventType eventType            // The type of the Event
    Location eventLocation        // The Location at which the Event occurred
    Date dateCreated
    Date lastUpdated

    static mapping = {
        id generator: 'uuid'
    }


    static constraints = {
        eventDate(nullable: true)
        eventType(nullable: true)
        eventLocation(nullable: true)
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
}
