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
import org.pih.warehouse.api.StockMovement
import org.pih.warehouse.api.StockMovementItem
import org.pih.warehouse.api.StockMovementType
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.Location
import org.pih.warehouse.importer.ImportDataCommand
import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.RequisitionSourceType
import org.pih.warehouse.requisition.RequisitionType

import java.text.SimpleDateFormat

class InboundStockMovementDataService {

    def shipmentService
    def inventoryService
    def stockMovementService

    void validateData(ImportDataCommand command) {
        buildStockMovements(command)
    }

    void importData(ImportDataCommand command) {
        buildStockMovements(command).each { StockMovement stockMovement ->
            if (stockMovement.validate()) {
                if (!stockMovement.id) {
                    stockMovementService.createStockMovement(stockMovement)
                }
                else {
                    stockMovementService.updateStockMovement(stockMovement)
                    stockMovementService.updateItems(stockMovement)
                }
            }
        }
    }

    List<StockMovement> buildStockMovements(ImportDataCommand command) {
        List<StockMovement> stockMovements = new ArrayList<StockMovement>()
        command.data.groupBy { it.loadCode }.each { loadCode, rows ->
            StockMovement stockMovement = buildStockMovement(command, rows[0])
            rows.each { row ->
                Product product = Product.findByProductCode(row.productCode)
                StockMovementItem existingStockMovementItem = stockMovement.lineItems.find { it.product == product }
                StockMovementItem stockMovementItem = buildStockMovementItem(command, row)
                stockMovementItem.id = existingStockMovementItem?.id
                stockMovementItem.stockMovement = stockMovement
                stockMovement.lineItems.add(stockMovementItem)
            }
            stockMovements.add(stockMovement)
        }
        return stockMovements
    }

    StockMovement buildStockMovement(ImportDataCommand command, Map params) {
        StockMovement stockMovement =
                stockMovementService.getStockMovementByIdentifier(params.loadCode)

        if (!stockMovement) {
            stockMovement = new StockMovement()
        }

        if (!isDateOneWeekFromNow(params.deliveryDate)) {
            command.errors.reject("Delivery date ${params.deliveryDate} for ${params.loadCode} must be more than seven (7) days away from today")
        }

        def expectedDeliveryDate =
                new SimpleDateFormat("yyyy-MM-dd").parse(params.deliveryDate.toString())

        stockMovement.identifier = params.loadCode
        stockMovement.description = ""
        stockMovement.stockMovementType = StockMovementType.INBOUND
        stockMovement.requestType = RequisitionType.DEFAULT
        stockMovement.sourceType = RequisitionSourceType.PAPER
        stockMovement.origin = findLocationByLocationNumber(params.origin)
        stockMovement.destination = findLocationByLocationNumber(params.destination)
        stockMovement.dateRequested = new Date()
        stockMovement.requestedBy = AuthService.currentUser.get()
        stockMovement.requestedDeliveryDate = expectedDeliveryDate
        stockMovement.expectedShippingDate = expectedDeliveryDate
        stockMovement.expectedDeliveryDate = expectedDeliveryDate
        return stockMovement
    }

    StockMovementItem buildStockMovementItem(ImportDataCommand command, Map params) {
        StockMovementItem stockMovementItem = new StockMovementItem()

        String productCode = params.productCode
        Product product = Product.findByProductCode(productCode)
        if(!product) {
            command.errors.reject("Product not found for product code ${productCode}")
        }

        def quantityRequested = params.quantity as Integer
        if (quantityRequested < 0) {
            command.errors.reject("Requested quantity (${quantityRequested}) for ${productCode} should be greater than or equal to 0")
        }

        stockMovementItem.product = product
        stockMovementItem.quantityRequested = quantityRequested
        stockMovementItem.comments = params.specialInstructions
        return stockMovementItem
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
