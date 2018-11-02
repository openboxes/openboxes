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

import grails.validation.ValidationException
import org.apache.commons.beanutils.BeanUtils
import org.pih.warehouse.api.AvailableItem
import org.pih.warehouse.api.Putaway
import org.pih.warehouse.api.PutawayItem
import org.pih.warehouse.api.PutawayStatus
import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationService
import org.pih.warehouse.core.LocationTypeCode
import org.pih.warehouse.core.User
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.InventoryService
import org.pih.warehouse.inventory.StockMovementService
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.inventory.TransferStockCommand
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.order.OrderItemStatusCode
import org.pih.warehouse.order.OrderStatus
import org.pih.warehouse.order.OrderTypeCode

class PutawayService {

    LocationService locationService
    InventoryService inventoryService

    boolean transactional = true

    def getPutawayCandidates(Location location) {
        List putawayItems = []
        List<Location> internalLocations = locationService.getInternalLocations(location,
                [ActivityCode.RECEIVE_STOCK] as ActivityCode[])

        log.info "internalLocations " + internalLocations
        internalLocations.each { internalLocation ->
            List putawayItemsTemp = inventoryService.getQuantityByBinLocation(location, internalLocation)
            if (putawayItemsTemp) {
                putawayItemsTemp = putawayItemsTemp.collect {

                    // FIXME Removed because it was causing very slow performance - determine if this is even necessary
                    List<AvailableItem> availableItems = []

                    PutawayItem putawayItem = new PutawayItem()
                    // FIXME Should be PENDING if there are existing putaways that are in-progress
                    putawayItem.putawayStatus = PutawayStatus.READY
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


    def completePutaway(Putaway putaway) {


        if (validatePutaway(putaway)) {

            // Save the putaway as a transfer order
            Order order = savePutaway(putaway)

            putaway.putawayItems.each { PutawayItem putawayItem ->
                TransferStockCommand command = new TransferStockCommand()
                command.location = putawayItem.currentFacility
                command.binLocation = putawayItem.currentLocation
                command.inventoryItem = putawayItem.inventoryItem
                command.quantity = putawayItem.quantity
                command.otherLocation = putawayItem.putawayFacility
                command.otherBinLocation = putawayItem.putawayLocation
                command.order = order
                command.transferOut = Boolean.TRUE

                Transaction transaction = inventoryService.transferStock(command)
                transaction.save(failOnError: true)
            }
        }

    }


    Order savePutaway(Putaway putaway) {

        Order order = new Order()
        order.orderTypeCode = OrderTypeCode.TRANSFER_ORDER
        order.description = "Putaway ${putaway.putawayNumber}"
        order.orderNumber = putaway.putawayNumber
        order.orderedBy = putaway.putawayAssignee
        order.completedBy = putaway.putawayAssignee
        order.dateOrdered = putaway.putawayDate?:new Date()
        order.status = OrderStatus.valueOf(putaway.putawayStatus.toString())
        order.origin = putaway.origin
        order.destination = putaway.destination

        putaway.putawayItems.toArray().each { PutawayItem putawayItem ->

            OrderItem orderItem = createOrderItem(putawayItem)
            order.addToOrderItems(orderItem)
            putawayItem.splitItems.each { PutawayItem splitItem ->
                OrderItem childOrderItem = createOrderItem(splitItem)
                childOrderItem.parentOrderItem = orderItem
                order.addToOrderItems(childOrderItem)
            }
        }

        order.save(failOnError: true)
        return order
    }


    OrderItem createOrderItem(PutawayItem putawayItem) {
        OrderItemStatusCode orderItemStatusCode =
                !putawayItem?.splitItems?.empty ? OrderItemStatusCode.CANCELED : OrderItemStatusCode.COMPLETED

        OrderItem orderItem = new OrderItem()
        orderItem.orderItemStatusCode = orderItemStatusCode
        orderItem.product = putawayItem.product
        orderItem.inventoryItem = putawayItem.inventoryItem
        orderItem.quantity = putawayItem.quantity
        orderItem.recipient = putawayItem.recipient
        orderItem.originBinLocation = putawayItem.currentLocation
        orderItem.destinationBinLocation = putawayItem.putawayLocation
        return orderItem
    }

    Boolean validatePutaway(Putaway putaway) {
        putaway.putawayItems.toArray().each { PutawayItem putawayItem ->
            if (putawayItem.splitItems) {
                putawayItem.splitItems.each { PutawayItem splitItem ->
                    validatePutawayItem(splitItem)
                }
            }
            else {
                validatePutawayItem(putawayItem)
            }
        }
        return true
    }


    Boolean validatePutawayItem(PutawayItem putawayItem) {
        return validateQuantityAvailable(putawayItem.currentFacility, putawayItem.currentLocation, putawayItem.inventoryItem, putawayItem.quantity)
    }


    Boolean validateQuantityAvailable(Location facility, Location internalLocation, InventoryItem inventoryItem, BigDecimal quantity) {

        if (!facility) {
            throw new IllegalArgumentException("Facility is required")
        }

        Integer quantityAvailable = inventoryService.getQuantity(facility?.inventory, internalLocation, inventoryItem)
        log.info "Quantity: ${quantity} vs ${quantityAvailable}"

        if (quantityAvailable < 0) {
            throw new IllegalStateException("The inventory item is no longer available at the specified facility ${facility} and bin ${internalLocation} ")
        }

        if (quantity > quantityAvailable) {
            throw new IllegalStateException("Quantity available ${quantityAvailable} is less than quantity to putaway ${quantity}")
        }

    }

}
