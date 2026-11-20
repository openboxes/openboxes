/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.replenishment

import org.springframework.context.ApplicationEvent

/**
 * A shortage reported on a replenishment pick: the source is the id of the picklist item that was shorted. Carries
 * what was done about it, so the listener can tell the world after the short pick is committed.
 */
class ReplenishmentShortageEvent extends ApplicationEvent {

    /** Quantity moved from the bin into the hold location (lost and found), 0 when nothing was moved */
    Integer quantityHeld
    /** The lost and found location the quantity was moved to; null when the facility has none */
    String holdLocationId
    /** Picklist items (replenishment tasks) created to pick the shorted quantity from other bins */
    List<String> reallocatedPicklistItemIds

    ReplenishmentShortageEvent(String picklistItemId, Integer quantityHeld, String holdLocationId,
                               List<String> reallocatedPicklistItemIds) {
        super(picklistItemId)
        this.quantityHeld = quantityHeld
        this.holdLocationId = holdLocationId
        this.reallocatedPicklistItemIds = reallocatedPicklistItemIds ?: []
    }

    String getPicklistItemId() {
        return (String) source
    }
}
