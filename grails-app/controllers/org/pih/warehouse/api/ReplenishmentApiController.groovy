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
import grails.validation.ValidationException
import org.codehaus.groovy.grails.web.json.JSONObject
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User
import org.pih.warehouse.inventory.InventoryLevelStatus
import org.pih.warehouse.inventory.Requirement
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.order.OrderType
import org.pih.warehouse.order.OrderTypeCode

class ReplenishmentApiController {

    def identifierService
    def replenishmentService

    def list = {
        List<Order> replenishments = Order.findAllByOrderType(OrderType.get(OrderTypeCode.TRANSFER_ORDER.name()))
        render([data: replenishments.collect { it.toJson() }] as JSON)
    }

    def read = {
        Order order = Order.get(params.id)
        if (!order) {
            throw new IllegalArgumentException("No replenishment found for order ID ${params.id}")
        }

        Replenishment replenishment = Replenishment.createFromOrder(order)
        replenishmentService.refreshQuantities(replenishment)
        render([data: replenishment?.toJson()] as JSON)
    }

    def create = {
        JSONObject jsonObject = request.JSON

        User currentUser = User.get(session.user.id)
        Location currentLocation = Location.get(session.warehouse.id)
        if (!currentLocation || !currentUser) {
            throw new IllegalArgumentException("User must be logged into a location to update replenishment")
        }

        Replenishment replenishment = new Replenishment()

        bindReplenishmentData(replenishment, currentUser, currentLocation, jsonObject)

        Order order = replenishmentService.createOrUpdateOrderFromReplenishment(replenishment)
        if (order.hasErrors() || !order.save(flush: true)) {
            throw new ValidationException("Invalid order", order.errors)
        }

        render status: 201
    }

    def update = {
        JSONObject jsonObject = request.JSON

        User currentUser = User.get(session.user.id)
        Location currentLocation = Location.get(session.warehouse.id)
        if (!currentLocation || !currentUser) {
            throw new IllegalArgumentException("User must be logged into a location to update replenishment")
        }

        Replenishment replenishment = new Replenishment()

        bindReplenishmentData(replenishment, currentUser, currentLocation, jsonObject)

        if (replenishment?.status == ReplenishmentStatus.COMPLETED) {
            replenishmentService.completeReplenishment(replenishment)
        } else {
            Order order = replenishmentService.createOrUpdateOrderFromReplenishment(replenishment)
            if (order.hasErrors() || !order.save(flush: true)) {
                throw new ValidationException("Invalid order", order.errors)
            }
        }

        render status: 200
    }

    Replenishment bindReplenishmentData(Replenishment replenishment, User currentUser, Location currentLocation, JSONObject jsonObject) {
        bindData(replenishment, jsonObject)

        if (!replenishment.origin) {
            replenishment.origin = currentLocation
        }

        if (!replenishment.destination) {
            replenishment.destination = currentLocation
        }

        if (!replenishment.orderedBy) {
            replenishment.orderedBy = currentUser
        }

        if (!replenishment.replenishmentNumber) {
            replenishment.replenishmentNumber = identifierService.generateOrderIdentifier()
        }

        jsonObject.replenishmentItems.each { replenishmentItemMap ->
            ReplenishmentItem replenishmentItem = new ReplenishmentItem()
            bindData(replenishmentItem, replenishmentItemMap)
            if (!replenishmentItem.location) {
                replenishmentItem.location = replenishment.destination
            }

            replenishmentItemMap.pickItems.each { pickItemMap ->
                ReplenishmentItem pickItem = new ReplenishmentItem()
                bindData(pickItem, pickItemMap)
                if (!pickItem.location) {
                    pickItem.location = replenishment.origin
                }
                replenishmentItem.picklistItems.add(pickItem)
            }

            replenishment.replenishmentItems.add(replenishmentItem)
        }

        return replenishment
    }

    def statusOptions = {
        def options = InventoryLevelStatus.listReplenishmentOptions()?.collect {
            [ id: it.name(), label: "${g.message(code: 'enum.InventoryLevelStatus.' + it.name())}" ]
        }
        render([data: options] as JSON)
    }

    def requirements = {
        Location location = Location.get(params.location.id)
        if (!location) {
            throw new IllegalArgumentException("Can't find location with given id: ${params.location.id}")
        }

        InventoryLevelStatus inventoryLevelStatus = InventoryLevelStatus.valueOf(params.inventoryLevelStatus) ?: InventoryLevelStatus.BELOW_MINIMUM
        List<Requirement> requirements = replenishmentService.getRequirements(location, inventoryLevelStatus)
        render([data: requirements?.collect { it.toJson() }] as JSON)
    }

    def removeItem = {
        replenishmentService.deleteReplenishmentItem(params.id)
        render status: 204
    }

    def createPicklist = {
        OrderItem orderItem = OrderItem.get(params.id)
        if (!orderItem) {
            throw new IllegalArgumentException("Can't find order item with given id: ${params.id}")
        }

//        TODO: OBAM-169: Remove user modified picklist for given orderItem
//        replenishmentService.removePicklist(orderItem)

//        TODO: OBAM-169: Create new auto picklist for given orderItem from suggested items
//        replenishmentService.createPicklist(orderItem)

        render status: 201
    }

    def updatePicklist = {
        OrderItem orderItem = OrderItem.get(params.id)
        if (!orderItem) {
            throw new IllegalArgumentException("Can't find order item with given id: ${params.id}")
        }

        JSONObject jsonObject = request.JSON
        List picklistItems = jsonObject.remove("picklistItems")

        if (!picklistItems) {
            throw new IllegalArgumentException("Must specifiy picklistItems")
        }

//        TODO: OBAM-169: Update picklist items made by user for given orderItem
//        replenishmentService.updatePicklist(orderItem, picklistItems)

        render status: 200
    }

    def deletePicklist = {
        OrderItem orderItem = OrderItem.get(params.id)
        if (!orderItem) {
            throw new IllegalArgumentException("Can't find order item with given id: ${params.id}")
        }

//        TODO: OBAM-169: Update picklist items made by user for given orderItem
//        replenishmentService.removePicklist(orderItem)

        render status: 200
    }
}
