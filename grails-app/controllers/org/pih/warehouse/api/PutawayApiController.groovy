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
import org.pih.warehouse.order.OrderType
import org.pih.warehouse.core.Constants
import org.pih.warehouse.order.OrderStatus

/**
 * Should not extend BaseDomainApiController since stocklist is not a valid domain.
 */
class PutawayApiController {

    def identifierService
    def orderService
    def productAvailabilityService
    def putawayService

    def list = {
        OrderType orderType = OrderType.findByCode(Constants.PUTAWAY_ORDER)
        OrderStatus status = params.status ? params.status as OrderStatus : OrderStatus.PENDING
        Order orderCriteria = new Order(orderType: orderType, status: status)
        List<Order> orders = orderService.getOrders(orderCriteria)
        List<Putaway> putaways = orders.collect {  Order order -> Putaway.createFromOrder(order) }
        render([data: putaways.collect { it.toJson() }] as JSON)
    }

    def read = {
        Order order = Order.findByIdOrOrderNumber(params.id, params.id)
        if (!order) {
            throw new IllegalArgumentException("No putaway found for order ID ${params.id}")
        }

        Putaway putaway = Putaway.createFromOrder(order)
        putaway.sortBy = params.sortBy
        putaway.putawayItems.each { PutawayItem putawayItem ->
            putawayItem.availableItems =
                    productAvailabilityService.getAllAvailableBinLocations(putawayItem.currentFacility, putawayItem.product)
            putawayItem.inventoryLevel = InventoryLevel.findByProductAndInventory(putawayItem.product, putaway.origin.inventory)
            putawayItem.quantityAvailable = productAvailabilityService.getQuantityOnHandInBinLocation(putawayItem.inventoryItem, putawayItem.currentLocation)
        }
        render([data: putaway?.toJson()] as JSON)
    }

    def delete = {
        Order order = Order.findByIdOrOrderNumber(params.id, params.id)
        if (!order) {
            throw new IllegalArgumentException("No putaway found for order ID ${params.id}")
        }
        order.delete()
        render status: 204
    }

    def create = {
        forward(action: "update")
    }

    def update = {
        JSONObject jsonObject = request.JSON

        Location currentLocation = Location.get(session.warehouse.id)
        if (!currentLocation) {
            throw new IllegalArgumentException("User must be logged into a location to perform putaway")
        }

        User currentUser = User.get(session.user.id)

        Putaway putaway = new Putaway()
        bindPutawayData(putaway, currentUser, currentLocation, jsonObject)
        Order order

        // Putaway stock
        if (putaway?.putawayStatus?.equals(PutawayStatus.COMPLETED)) {
            order = putawayService.completePutaway(putaway)
        } else {
            order = putawayService.savePutaway(putaway)
        }
        redirect(action: "read", id: order.id)
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
