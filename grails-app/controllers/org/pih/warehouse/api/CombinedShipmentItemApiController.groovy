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
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.product.Product
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem

class CombinedShipmentItemApiController {

    def getOrderOptions = {
        def vendor = Location.get(params.vendor)
        def destination = Location.get(params.destination)
        List<Order> orders = Order.findAllByOriginAndDestination(vendor, destination)
        render([data: orders.collect {
            [
                value: it.id,
                label: it.orderNumber
            ]
        }] as JSON)
    }

    def findOrderItems = {
        List<Order> orders = Order.findAllByIdInList(params.orderIds)
        Product product = Product.get(params.productId)

        def orderItems

        if (orders && product) {
            orderItems = OrderItem.findAllByOrderInListAndProduct(orders, product)
        } else if (orders) {
            orderItems = OrderItem.findAllByOrderInList(orders)
        } else if (product) {
            orderItems = OrderItem.findAllByProduct(product)
        } else {
            orderItems = []
        }

        render([orderItems: orderItems.findAll{ it.getQuantityRemainingToShip() > 0 }.collect {
            [
                orderId: it.order.id,
                orderItemId: it.id,
                orderNumber: it.order?.orderNumber,
                productCode: it.product?.productCode,
                productName: it.product?.name,
                budgetCode: it.budgetCode?.code,
                recipient: it.recipient?.name,
                quantityAvailable: it.getQuantityRemainingToShip(),
                quantityToShip: '',
                uom: it.unitOfMeasure
            ]
        }] as JSON)
    }

    def addItemsToShipment = {
        JSONObject jsonObject = request.JSON
        Shipment shipment = Shipment.get(params.id)
        if (!shipment) {
            render(status: 400, text: "Shipment not found")
            return
        }
        List itemsToAdd = jsonObject.itemsToAdd
        if (itemsToAdd) {
            itemsToAdd.each {
                OrderItem orderItem = OrderItem.get(it.orderItemId)
                ShipmentItem shipmentItem = new ShipmentItem()
                shipmentItem.product = orderItem.product
                shipmentItem.inventoryItem = orderItem.inventoryItem
                shipmentItem.product = orderItem.product
                shipmentItem.quantity = orderItem.quantity
                shipmentItem.recipient = orderItem.recipient
                shipmentItem.quantity = it.quantityToShip * orderItem.quantityPerUom
                shipmentItem.save()
                orderItem.addToShipmentItems(shipmentItem)
                orderItem.save()
                shipment.addToShipmentItems(shipmentItem)
            }
            shipment.save()
        }
        render([data: shipment] as JSON)
    }
}
