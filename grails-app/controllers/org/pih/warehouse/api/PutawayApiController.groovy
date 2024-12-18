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
import grails.plugins.rendering.pdf.PdfRenderingService
import org.grails.web.json.JSONObject
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.User
import org.pih.warehouse.inventory.InventoryLevel
import org.pih.warehouse.core.Location
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderIdentifierService
import org.pih.warehouse.order.OrderStatus
import org.pih.warehouse.order.OrderType

/**
 * Should not extend BaseDomainApiController since stocklist is not a valid domain.
 */
class PutawayApiController {

    OrderIdentifierService orderIdentifierService
    def productAvailabilityService
    def putawayService
    def orderService
    PdfRenderingService pdfRenderingService

    def list() {
        String locationId = params?.location?.id ?: session?.warehouse?.id
        Location location = Location.get(locationId)
        if (!location) {
            throw new IllegalArgumentException("Must provide location.id as request parameter")
        }
        List putawayItems = putawayService.getPutawayCandidates(location)
        render([data: putawayItems.collect { it.toJson() }] as JSON)
    }

    def listPutaways() {
        // MOBILE
        Location location = params["location.id"] ? Location.get(params["location.id"]) : Location.load(session.warehouse.id)
        if (!location) {
            throw new IllegalArgumentException("Must provide location.id as request parameter")
        }
        OrderType orderType = OrderType.findByCode(Constants.PUTAWAY_ORDER)
        OrderStatus status = params.status ? params.status as OrderStatus : OrderStatus.PENDING
        Order orderCriteria = new Order(orderType: orderType, status: status, origin: location, destination: location)
        List<Order> orders = orderService.getOrders(orderCriteria)
        List<Putaway> putaways = orders.collect {  Order order -> Putaway.createFromOrder(order) }
        render([data: putaways.collect { it.toJson() }] as JSON)
    }

    def read() {
        Order order = Order.get(params.id)
        if (!order) {
            throw new IllegalArgumentException("No putaway found for order ID ${params.id}")
        }

        Putaway putaway = Putaway.createFromOrder(order)
        putaway.sortBy = params.sortBy
        putaway.putawayItems.each { PutawayItem putawayItem ->
            putawayItem.availableItems =
                    productAvailabilityService.getAllAvailableBinLocations(putawayItem.currentFacility, putawayItem.product?.id)
            putawayItem.inventoryLevel = InventoryLevel.findByProductAndInventory(putawayItem.product, putaway.origin.inventory)
            putawayItem.quantityAvailable = productAvailabilityService.getQuantityOnHandInBinLocation(putawayItem.inventoryItem, putawayItem.currentLocation)
        }
        render([data: putaway?.toJson()] as JSON)
    }


    def create() {
        JSONObject jsonObject = request.JSON

        Location currentLocation = Location.get(session.warehouse.id)
        if (!currentLocation) {
            throw new IllegalArgumentException("User must be logged into a location to perform putaway")
        }

        Order order = Order.get(jsonObject.id)
        if (order && Putaway.getPutawayStatus(order.status) == PutawayStatus.COMPLETED) {
            throw new IllegalArgumentException("Can't update completed putaway")
        }

        User currentUser = User.get(session.user.id)

        Putaway putaway = new Putaway()
        bindPutawayData(putaway, order, currentUser, currentLocation, jsonObject)

        // Putaway stock
        if (putaway?.putawayStatus?.equals(PutawayStatus.COMPLETED)) {
            order = putawayService.completePutaway(putaway)
            putaway = Putaway.createFromOrder(order)
        } else {
            order = putawayService.savePutaway(putaway)
            putaway = Putaway.createFromOrder(order)
            putaway.sortBy = jsonObject.sortBy
            putaway?.putawayItems?.each { PutawayItem putawayItem ->
                putawayItem.availableItems =
                        productAvailabilityService.getAllAvailableBinLocations(putawayItem.currentFacility, putawayItem.product?.id)
                putawayItem.inventoryLevel = InventoryLevel.findByProductAndInventory(putawayItem.product, putaway.origin.inventory)
                putawayItem.quantityAvailable = productAvailabilityService.getQuantityOnHandInBinLocation(putawayItem.inventoryItem, putawayItem.currentLocation)
            }
        }
        render([data: putaway?.toJson()] as JSON)
    }


    private Putaway bindPutawayData(Putaway putaway, Order order, User currentUser, Location currentLocation, JSONObject jsonObject) {
        // Bind the putaway
        bindData(putaway, jsonObject)

        if (!putaway.origin) {
            putaway.origin = currentLocation
        }
        if (!putaway.destination) {
            putaway.destination = currentLocation
        }

        if (!putaway.putawayNumber) {
            putaway.putawayNumber = orderIdentifierService.generate(order)
        }

        putaway.putawayAssignee = currentUser

        return putaway
    }

}
