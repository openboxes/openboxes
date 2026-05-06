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
 * Represents the subset of {@link EventCode} that are supported by the specific application instance.
 *
 * This is useful because it allows implementations to:
 * 1) exclude some event codes. For example, an implementation doesn't distinguish between PICKED vs PACKED.
 * 2) create their own custom event types (via the CUSTOM event code).
 * 3) define their own custom sort order for event types.
 *
 * As such, EventType can be seen as a wrapper on EventCode. EventType should have a one-to-one mapping
 * to EventCode, *except* for the CUSTOM event code, which can have multiple event types.
 */
class EventType implements Comparable<EventType>, Serializable {

    String id
    String name
    String description
    Integer sortOrder = 0
    Date dateCreated
    Date lastUpdated

    EventCode eventCode

    /**
     * True if the event type is enabled by the application instance.
     *
     * We add this field so that implementations can disable the event type while still being able to see it
     * in event type list views. This is especially useful for system event types, which will be created
     * and enabled by default.
     */
    boolean active = true

    static transients = ["optionValue"]
    static constraints = {
        name(nullable: false, maxSize: 255)
        description(nullable: true, maxSize: 255)
        sortOrder(nullable: true)
        eventCode(nullable: false, validator: { EventCode eventCode, EventType eventType ->
            if (eventType.isDirty("eventCode") && !eventCode.customEvent && countByEventCode(eventCode) > 0) {
                return ["invalid.notUnique", eventCode]
            }
            return true
        })
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

    static List<EventType> listCustomEventTypes() {
        return createCriteria().list {
            'in'('eventCode', EventCode.listCustomEventTypeCodes())
            eq("active", true)
        }
    }
}
