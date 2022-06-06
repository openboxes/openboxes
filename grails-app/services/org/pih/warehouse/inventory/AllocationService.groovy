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

import org.pih.warehouse.api.AvailableItem
import org.pih.warehouse.api.SuggestedItem
import org.pih.warehouse.core.Location
import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.RequisitionItem

class AllocationService {

    def stockMovementService
    def productAvailabilityService

    boolean transactional = true

    List<AvailableItem> getAvailableItems(Location location, Product product, Integer quantityRequired) {
        List<AvailableItem> availableItems = productAvailabilityService.getAllAvailableBinLocations(location, product)
        //def picklistItems = getPicklistItems(requisitionItem)

        availableItems = availableItems.findAll { it.quantityOnHand > 0 }
        //availableItems = calculateQuantityAvailableToPromise(availableItems, picklistItems)

        //if (calculateStatus) {
        //    return calculateAvailableItemsStatus(requisitionItem, availableItems)
        //}

        return productAvailabilityService.sortAvailableItems(availableItems, quantityRequired)
    }

    List<SuggestedItem> getAllocatedItems(Location location, Product product, Integer quantityRequired) {
        List<SuggestedItem> allocatedItems = []
        while (quantityRequired > 0) {
            log.info "quantityRequired " + quantityRequired
            List<AvailableItem> availableItems = getAvailableItems(location, product, quantityRequired)
            List<SuggestedItem> suggestedItems = stockMovementService.getSuggestedItems(availableItems, quantityRequired)
            if (suggestedItems.empty) {
                break;
            }

            suggestedItems.removeAll(allocatedItems)

            SuggestedItem allocatedItem = suggestedItems.first()
            allocatedItems << allocatedItem
            quantityRequired -= allocatedItem.quantityToPick
            log.info "quantityRequired " + quantityRequired
        }
        return allocatedItems
    }
}
