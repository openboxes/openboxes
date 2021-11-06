/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/
package org.pih.warehouse.data

import org.joda.time.LocalDate
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.EventTypeCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.importer.ImportDataCommand
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.product.Product
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem
import org.pih.warehouse.shipping.ShipmentType
import org.springframework.validation.BeanPropertyBindingResult

import java.text.SimpleDateFormat

class InboundStockMovementDataService {

    def shipmentService
    def inventoryService

    Boolean validateData(ImportDataCommand command) {
        log.info "Validate data " + command.filename
        command.data.eachWithIndex { params, index ->
            if (!params?.origin) {
                throw new IllegalArgumentException("Row ${index + 1}: Origin is required")
            }
            if (!params?.quantity) {
                throw new IllegalArgumentException("Row ${index + 1}: Quantity is required")
            }
            ShipmentItem shipmentItem = buildShipmentItem(params, command.location)
            if (!shipmentItem.validate()) {
                shipmentItem.errors.each { BeanPropertyBindingResult error ->
                    command.errors.reject("${index + 1}: ${shipmentItem} ${error.getFieldError()}")
                }
            }
        }
    }

    void importData(ImportDataCommand command) {
        log.info "Import data " + command.filename
        command.data.eachWithIndex {params, index ->
            ShipmentItem shipmentItem = buildShipmentItem(params, command.location)
            if(shipmentItem.validate()){
                shipmentItem.save(failOnError: true)
            }
        }
    }

    ShipmentItem buildShipmentItem(Map params, Location location) {

        log.info "Build shipment item " + params

        String productCode = params.productCode
        Product product = Product.findByProductCode(productCode)
        if(!product) {
            throw new IllegalArgumentException("Product not found for ${productCode}")
        }

        def quantity = params.quantity as Integer
        if (!(quantity > 0)) {
            throw new IllegalArgumentException("Requested quantity should be greater than 0")
        }

        if (!isDateOneWeekFromNow(params.deliveryDate)) {
            throw new IllegalArgumentException("Delivery date must be after seven days from now")
        }

        def expectedDeliveryDate =
                new SimpleDateFormat("yyyy-mm-dd").parse(params.deliveryDate.toString())

        def shipmentNumber = params.shipmentNumber
        def shipment = Shipment.findByShipmentNumber(shipmentNumber)
        if (!shipment) {
            shipment = new Shipment()
            shipment.name = "Inbound Order ${shipmentNumber}"
            shipment.description = "Inbound Order ${shipmentNumber}"
            shipment.origin = findLocationByLocationNumber(params.origin)
            shipment.destination = findLocationByLocationNumber(params.destination)
            shipment.expectedShippingDate = expectedDeliveryDate
            shipment.expectedDeliveryDate = expectedDeliveryDate
            shipment.shipmentType = ShipmentType.get(Constants.DEFAULT_SHIPMENT_TYPE_ID)
            shipment.shipmentNumber = shipmentNumber
            shipment.save(failOnError: true)
            shipmentService.createShipmentEvent(shipment, new Date(), EventTypeCode.CREATED, location)
        }

        ShipmentItem shipmentItem = ShipmentItem.createCriteria().get {
            eq "product" , product
            eq "shipment", shipment
        }

        if (!shipmentItem) {
            shipmentItem = new ShipmentItem()
        }

        InventoryItem inventoryItem =
                inventoryService.findOrCreateInventoryItem(product, null, null)

        shipmentItem.product = product
        shipmentItem.inventoryItem = inventoryItem
        shipmentItem.quantity = quantity
        shipment.addToShipmentItems(shipmentItem)

        return shipmentItem
    }

    Location findLocationByLocationNumber(String locationNumber) {
        Location location = Location.findByLocationNumber(locationNumber)
        if (!location) {
            throw new IllegalArgumentException("Location not found for location number ${locationNumber}")
        }
        return location
    }


    boolean isDateOneWeekFromNow(def date) {
        LocalDate today = LocalDate.now()
        LocalDate oneWeekFromNow = today.plusDays(7)
        if(date > oneWeekFromNow) {
            return true
        }
        return false
    }
}
