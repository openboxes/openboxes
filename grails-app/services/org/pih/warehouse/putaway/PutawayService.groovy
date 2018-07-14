/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.putaway

import org.apache.commons.beanutils.BeanUtils
import org.pih.warehouse.api.AvailableItem
import org.pih.warehouse.api.Putaway
import org.pih.warehouse.api.PutawayItem
import org.pih.warehouse.api.PutawayStatus
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.TransferStockCommand

class PutawayService {

    def locationService
    def inventoryService
    def stockMovementService

    boolean transactional = true

    def getPutawayCandidates(Location location) {
        List putawayItems = []
        List<Location> internalLocations = locationService.getInternalLocations(location, [ActivityCode.RECEIVE_STOCK] as ActivityCode[])
        log.info "internalLocations " + internalLocations
        internalLocations.each { internalLocation ->
            List putawayItemsTemp = inventoryService.getQuantityByBinLocation(location, internalLocation)
            if (putawayItemsTemp) {
                putawayItemsTemp = putawayItemsTemp.collect {

                    List<AvailableItem> availableItems =
                            inventoryService.getAvailableBinLocations(location, it.product)

                    PutawayItem putawayItem = new PutawayItem()
                    // FIXME Should be PENDING if there are existing putaways that are in-progress
                    putawayItem.putawayStatus = PutawayStatus.TODO
                    putawayItem.product = it.product
                    putawayItem.inventoryItem = it.inventoryItem
                    putawayItem.currentFacility = location
                    putawayItem.currentLocation = it.binLocation
                    putawayItem.putawayFacility = null
                    putawayItem.putawayLocation = null
                    putawayItem.availableItems = availableItems
                    putawayItem.quantity = it.quantity
                    return putawayItem
                }
                putawayItems.addAll(putawayItemsTemp)
            }
        }
        return putawayItems
    }

    void processSplitItems(Putaway putaway) {

        putaway.putawayItems.toArray().each { PutawayItem oldPutawayItem ->

            if (oldPutawayItem.splitItems) {

                Integer totalSplitQuantity = oldPutawayItem.splitItems.sum { it.quantity }
                // Validate quantity
                if (totalSplitQuantity != oldPutawayItem?.quantity) {
                    throw new IllegalArgumentException("Sum of split quantities " +
                            "must equal original quantity [${totalSplitQuantity} != ${oldPutawayItem?.quantity}]")
                }

                // Iterate over split items and create new putaway items for them
                // NOTE: The only fields we change from the original are the putaway bin and quantity.
                oldPutawayItem.splitItems.each { PutawayItem splitPutawayItem ->
                    PutawayItem newPutawayItem = new PutawayItem()
                    BeanUtils.copyProperties(newPutawayItem, oldPutawayItem)
                    newPutawayItem.quantity = splitPutawayItem.quantity
                    newPutawayItem.putawayFacility = splitPutawayItem.putawayFacility
                    newPutawayItem.putawayLocation = splitPutawayItem.putawayLocation
                    putaway.putawayItems.add(newPutawayItem)
                }

                // Remove the original putaway item since it was replaced with the above
                putaway.putawayItems.remove(oldPutawayItem)
            }

        }
    }


    def putawayStock(Putaway putaway) {
        putaway.putawayItems.each { PutawayItem putawayItem ->
            TransferStockCommand command = new TransferStockCommand()
            command.location = putawayItem.currentFacility
            command.binLocation = putawayItem.currentLocation
            command.inventoryItem = putawayItem.inventoryItem
            command.quantity = putawayItem.quantity
            command.otherLocation = putawayItem.putawayFacility
            command.otherBinLocation = putawayItem.putawayLocation
            command.transferOut = Boolean.TRUE
            putawayItem.transaction = inventoryService.transferStock(command)
        }

    }

}
