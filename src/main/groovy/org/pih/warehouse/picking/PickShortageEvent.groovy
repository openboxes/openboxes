package org.pih.warehouse.picking

import org.pih.warehouse.core.Location
import org.pih.warehouse.picklist.PicklistItem
import org.springframework.context.ApplicationEvent

/**
 * Published when a picker records a shortage (a reason code) on a pick task.
 */
class PickShortageEvent extends ApplicationEvent {

    Integer quantityMovedToLostAndFound
    Location lostAndFound

    PickShortageEvent(PicklistItem picklistItem, Integer quantityMovedToLostAndFound, Location lostAndFound) {
        super(picklistItem)
        this.quantityMovedToLostAndFound = quantityMovedToLostAndFound
        this.lostAndFound = lostAndFound
    }

    PicklistItem getPicklistItem() {
        return (PicklistItem) source
    }
}
