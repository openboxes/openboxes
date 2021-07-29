/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.inventory

import org.pih.warehouse.core.Location
import org.pih.warehouse.picklist.PicklistItem
import org.springframework.context.ApplicationEvent

class RefreshProductAvailabilityEvent extends ApplicationEvent {

    String locationId
    List productIds
    Boolean forceRefresh = Boolean.FALSE
    Boolean disableRefresh = Boolean.FALSE
    Boolean synchronousRequired = Boolean.FALSE

    RefreshProductAvailabilityEvent(Transaction source) {
        super(source)
        this.locationId = source.associatedLocation
        this.productIds = source.associatedProducts
        this.forceRefresh = source.forceRefresh
        this.disableRefresh = source.disableRefresh
    }

    RefreshProductAvailabilityEvent(Location source) {
        super(source)
        this.locationId = source?.isBinLocation() ?source?.parentLocation?.id : source?.id
        this.productIds = []
        this.forceRefresh = forceRefresh
        this.disableRefresh = disableRefresh
    }

    RefreshProductAvailabilityEvent(InventoryItem source) {
        super(source)
        this.locationId = null
        this.productIds = source.associatedProducts
    }

    RefreshProductAvailabilityEvent(PicklistItem source) {
        super(source)
        this.locationId = source.associatedLocation
        this.productIds = source.associatedProducts
        this.synchronousRequired = source.synchronousRequired
    }

    RefreshProductAvailabilityEvent(Transaction source, Boolean forceRefresh) {
        super(source)
        this.locationId = source.associatedLocation
        this.productIds = source.associatedProducts
        this.disableRefresh = source.disableRefresh
        this.forceRefresh = forceRefresh
    }

    RefreshProductAvailabilityEvent(Transaction source, String locationId, List<String> productIds, Boolean forceRefresh) {
        super(source)
        this.locationId = locationId
        this.productIds = productIds
        this.disableRefresh = disableRefresh
        this.forceRefresh = forceRefresh
    }
}
