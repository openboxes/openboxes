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
import org.pih.warehouse.inventory.StockMovementStatusCode
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

        if (!params?.origin) {
            command.errors.reject("Order number ${params.loadCode} failed: Origin is required")
        }
        if (!params?.destination) {
            command.errors.reject("Order number ${params.loadCode} failed: Destination is required")
        }
        if (params?.quantity < 0) {
            command.errors.reject("Order number ${params.loadCode} failed: Requested Quantity is required")
        }
        if(command?.location?.locationNumber != params.destination) {
            command.errors.reject("Order number ${params.loadCode} failed: Destination [${params.destination}] must match the current location [${command?.location?.locationNumber}]")
        }

        Location origin = Location.findByLocationNumber(params.origin)
        log.info "origin ${params.origin} " + origin
        if (!origin) {
            command.errors.reject("Order number ${params.loadCode} failed: Unknown Supplier [${params.origin}]")
        }

        Location destination = Location.findByLocationNumber(params.destination)
        log.info "destination ${params.destination} " + destination
        if (!destination) {
            command.errors.reject("Order number ${params.loadCode} failed: Unknown Destination [${params.destination}]")
        }


        if (stockMovement.stockMovementStatusCode >= StockMovementStatusCode.DISPATCHED) {
            command.errors.reject("Order number ${params.loadCode} failed: Cannot update an inbound stock movement with status ${stockMovement?.stockMovementStatusCode}")
        }

        def expectedDeliveryDate =
                new SimpleDateFormat("yyyy-MM-dd").parse(params.deliveryDate.toString())

        if (expectedDeliveryDate.before(new Date())) {
            command.errors.reject("Order number ${params.loadCode} failed: Expected delivery date should not be in the past")
        }

        stockMovement.identifier = params.loadCode
        stockMovement.description = ""
        stockMovement.stockMovementType = StockMovementType.INBOUND
        stockMovement.requestType = RequisitionType.DEFAULT
        stockMovement.sourceType = RequisitionSourceType.PAPER
        stockMovement.origin = origin
        stockMovement.destination = destination
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
            command.errors.reject("Order number ${params.loadCode} failed: Unknown SKU [${productCode}]")
        }

        def quantityRequested = params.quantity as Integer
        if (quantityRequested < 0) {
            command.errors.reject("Order number ${params.loadCode} failed: Requested quantity [${quantityRequested}] for SKU ${productCode} should be greater than or equal to 0")
        }

        stockMovementItem.product = product
        stockMovementItem.quantityRequested = quantityRequested
        stockMovementItem.comments = params.specialInstructions
        return stockMovementItem
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
