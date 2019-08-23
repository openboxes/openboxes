/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.api

import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONObject
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User
import org.pih.warehouse.inventory.InventoryLevel
import org.pih.warehouse.order.Order

/**
 * Should not extend BaseDomainApiController since stocklist is not a valid domain.
 */
class PutawayApiController {

    def putawayService
    def inventoryService
    def identifierService
    def pdfRenderingService

    def list = {
        String locationId = params?.location?.id ?: session?.warehouse?.id
        Location location = Location.get(locationId)
        if (!location) {
            throw new IllegalArgumentException("Must provide location.id as request parameter")
        }
        List putawayItems = putawayService.getPutawayCandidates(location)
        render([data: putawayItems.collect { it.toJson() }] as JSON)
    }

    def read = {
        Order order = Order.get(params.id)
        if (!order) {
            throw new IllegalArgumentException("No putaway found for order ID ${params.id}")
        }

        Putaway putaway = Putaway.createFromOrder(order)
        putaway.sortBy = params.sortBy
        putaway.putawayItems.each { PutawayItem putawayItem ->
            putawayItem.availableItems =
                    inventoryService.getAvailableBinLocations(putawayItem.currentFacility, putawayItem.product)
            putawayItem.inventoryLevel = InventoryLevel.findByProductAndInventory(putawayItem.product, putaway.origin.inventory)
            putawayItem.quantityAvailable = inventoryService.getQuantity(putawayItem.currentFacility.inventory, putawayItem.currentLocation, putawayItem.inventoryItem)
        }
        render([data: putaway?.toJson()] as JSON)
    }


    def create = { Putaway putaway ->
        JSONObject jsonObject = request.JSON

        Location currentLocation = Location.get(session.warehouse.id)
        if (!currentLocation) {
            throw new IllegalArgumentException("User must be logged into a location to perform putaway")
        }

        User currentUser = User.get(session.user.id)

        bindPutawayData(putaway, currentUser, currentLocation, jsonObject)
        Order order

        // Putaway stock
        if (putaway?.putawayStatus?.equals(PutawayStatus.COMPLETED)) {
            order = putawayService.completePutaway(putaway)
        } else {
            order = putawayService.savePutaway(putaway)
        }

        putaway = Putaway.createFromOrder(order)
        putaway.sortBy = jsonObject.sortBy
        putaway?.putawayItems?.each { PutawayItem putawayItem ->
            putawayItem.availableItems =
                    inventoryService.getAvailableBinLocations(putawayItem.currentFacility, putawayItem.product)
            putawayItem.inventoryLevel = InventoryLevel.findByProductAndInventory(putawayItem.product, putaway.origin.inventory)
            putawayItem.quantityAvailable = inventoryService.getQuantity(putawayItem.currentFacility.inventory, putawayItem.currentLocation, putawayItem.inventoryItem)
        }

        render([data: putaway?.toJson()] as JSON)
    }


    Putaway bindPutawayData(Putaway putaway, User currentUser, Location currentLocation, JSONObject jsonObject) {
        // Bind the putaway
        bindData(putaway, jsonObject)

        if (!putaway.origin) {
            putaway.origin = currentLocation
        }
        if (!putaway.destination) {
            putaway.destination = currentLocation
        }

        if (!putaway.putawayNumber) {
            putaway.putawayNumber = identifierService.generateOrderIdentifier()
        }

        putaway.putawayAssignee = currentUser

        // Bind the putaway items
        jsonObject.putawayItems.each { putawayItemMap ->
            PutawayItem putawayItem = new PutawayItem()
            bindData(putawayItem, putawayItemMap)

            // Bind the split items
            putawayItemMap.splitItems.each { splitItemMap ->
                PutawayItem splitItem = new PutawayItem()
                bindData(splitItem, splitItemMap)
                putawayItem.splitItems.add(splitItem)
            }

            putaway.putawayItems.add(putawayItem)
        }

        return putaway
    }


}
