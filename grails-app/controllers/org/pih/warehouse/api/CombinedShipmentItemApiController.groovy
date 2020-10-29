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
import org.pih.warehouse.importer.ImportDataCommand
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.order.OrderItemStatusCode
import org.pih.warehouse.product.Product
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem
import javax.xml.bind.ValidationException

class CombinedShipmentItemApiController {

    def orderService
    def combinedShipmentService

    def getProductsInOrders = {
        def minLength = grailsApplication.config.openboxes.typeahead.minLength
        def vendor = Location.get(params.vendor)
        def destination = Location.get(params.destination)
        String[] terms = params?.name?.split(",| ")?.findAll { it }
        if (params.name && params.name.size() < minLength) {
            render([data: []])
            return
        }
        def orderItems = orderService.getProductsInOrders(terms, destination, vendor)
        def products = orderItems*.findAll{ it.getQuantityRemainingToShip() > 0 }.flatten().toArray().collect { it.product }.unique()

        render([data: products] as JSON)
    }

    def getOrderOptions = {
        def vendor = Location.get(params.vendor)
        def destination = Location.get(params.destination)
        List<Order> orders = orderService.getOrdersForCombinedShipment(vendor, destination)
        render([data: orders.findAll{ it.orderItems.any { item -> item.getQuantityRemainingToShip() > 0 } }.collect {
            [
                value: it.id,
                label: it.orderNumber
            ]
        }] as JSON)
    }

    def findOrderItems = {
        List<Order> orders
        if (params.orderIds) {
            orders = Order.findAllByIdInList(params.orderIds)
        } else {
            def vendor = Location.get(params.vendor)
            def destination = Location.get(params.destination)
            orders = orderService.getOrdersForCombinedShipment(vendor, destination)
        }
        Product product = Product.get(params.productId)

        def orderItems

        if (product) {
            orderItems = OrderItem.findAllByOrderInListAndProduct(orders, product)
        } else  {
            orderItems = OrderItem.findAllByOrderInList(orders)
        }

        render([orderItems: orderItems.findAll{ it.orderItemStatusCode != OrderItemStatusCode.CANCELED && it.getQuantityRemainingToShip() > 0 }.collect {
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
                shipmentItem.sortOrder = it.sortOrder
                orderItem.addToShipmentItems(shipmentItem)
                shipment.addToShipmentItems(shipmentItem)
            }
            shipment.save()
        }
        render([data: shipment] as JSON)
    }

    def importTemplate = { ImportDataCommand command ->
        Shipment shipment = Shipment.get(params.id)
        def importFile = command.importFile
        if (importFile.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty")
        }

        if (importFile.fileItem.contentType != "text/csv") {
            throw new IllegalArgumentException("File must be in CSV format")
        }
        String csv = new String(importFile.bytes)
        List importedLines = combinedShipmentService.parseOrderItemsFromTemplateImport(csv)
        if (combinedShipmentService.validateItemsFromTemplateImport(shipment, importedLines)) {
            combinedShipmentService.addItemsToShipment(shipment, importedLines)
        } else {
            String message = "Failed to import template due to validation errors:"
            importedLines.eachWithIndex { line, idx ->
                if (line.errors) {
                    message += "<br>Row ${idx + 1}: ${line.errors.join("; ")}"
                }
            }
            throw new ValidationException(message)
        }
        render (status: 200, text: "Successfully imported template")
    }
}
