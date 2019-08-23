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
 * Represents the type of an Event
 *
 * This is distinct from ShipmentStatus in that status is meant to reflect the overall
 * status of a Shipment from Supplier to final destination, whereas ShipmentEvent is
 * meant to represent a particular Event which occurs during the course of Shipment.
 */
class EventType implements Comparable<EventType>, Serializable {

    String id
    String name
    String description
    Integer sortOrder = 0
    Date dateCreated
    Date lastUpdated

    EventCode eventCode        // CREATED, SHIPPED or RECEIVED

    static transients = ["optionValue"]
    static constraints = {
        name(nullable: false, maxSize: 255)
        description(nullable: true, maxSize: 255)
        sortOrder(nullable: true)
        eventCode(nullable: false)
        dateCreated(display: false)
        lastUpdated(display: false)
        optionValue(display: false)
    }

    static mapping = {
        id generator: 'uuid'
        sort "sortOrder"
    }

    String getOptionValue() {
        return (description) ? description : name
    }


    String toString() { return "$name" }


    int compareTo(EventType other) {
        return sortOrder <=> other?.sortOrder ?: eventCode <=> other?.eventCode ?: name <=> other?.name
    }
}
