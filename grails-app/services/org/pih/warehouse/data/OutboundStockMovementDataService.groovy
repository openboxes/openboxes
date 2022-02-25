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

import org.pih.warehouse.api.StockMovement
import org.pih.warehouse.api.StockMovementItem
import org.pih.warehouse.api.StockMovementType
import org.pih.warehouse.core.Location
import org.pih.warehouse.importer.ImportDataCommand
import org.pih.warehouse.inventory.StockMovementStatusCode
import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.RequisitionSourceType
import org.pih.warehouse.requisition.RequisitionType
import org.pih.warehouse.auth.AuthService

import java.text.SimpleDateFormat

class OutboundStockMovementDataService {

    def grailsApplication
    def notificationService
    def stockMovementService
    def tmsIntegrationService

    Boolean validateData(ImportDataCommand command) {
        buildStockMovements(command)
    }

    void importData(ImportDataCommand command) {
        List<String> identifiers = buildStockMovements(command).collect { StockMovement stockMovement ->
            if (stockMovement.validate()) {
                if (!stockMovement.id) {
                    stockMovementService.createStockMovement(stockMovement)
                }
                else {
                    stockMovementService.updateStockMovement(stockMovement)
                    stockMovementService.updateItems(stockMovement)
                }
            }
            return stockMovement?.identifier
        }

        // Generate hypothetical delivery order for newly created stock movements
        Boolean uploadDeliveryOrderOnCreate = grailsApplication.config.openboxes.integration.uploadDeliveryOrderOnCreate.enabled
        if (uploadDeliveryOrderOnCreate) {
            if (identifiers) {
                identifiers.each { String identifier ->
                    StockMovement stockMovement = stockMovementService.getStockMovementByIdentifier(identifier, Boolean.FALSE)
                    tmsIntegrationService.uploadDeliveryOrder(stockMovement)
                }
            }
        }
    }

    List<StockMovement> buildStockMovements(ImportDataCommand command) {
        Integer index = 0
        List<StockMovement> stockMovements = new ArrayList<StockMovement>()
        command.data.groupBy { it.identifier }.each { identifier, rows ->
            StockMovement stockMovement = buildStockMovement(command, rows[0], index)
            rows.each { row ->
                // Create or update stock movement item
                Product product = Product.findByProductCode(row.productCode)
                StockMovementItem existingStockMovementItem = stockMovement.lineItems.find { it.product == product }
                StockMovementItem stockMovementItem = buildStockMovementItem(command, row, index)
                stockMovementItem.id = existingStockMovementItem?.id
                stockMovementItem.stockMovement = stockMovement
                stockMovement.lineItems.add(stockMovementItem)
                index++
            }
            stockMovements.add(stockMovement)
        }
        return stockMovements
    }

    StockMovement buildStockMovement(ImportDataCommand command, Map params, Integer index) {

        log.info "Build stock movement " + params

        StockMovement stockMovement =
                stockMovementService.getStockMovementByIdentifier(params.identifier)

        if (command.id) {
            if (!stockMovement) {
                command.errors.reject("Row ${index + 1}: Order number ${params.identifier}: Cannot locate order with identifier ${params.identifier}")
            }

            StockMovement targetStockMovement = stockMovementService.getStockMovement(command.id)
            if (!stockMovement || command.id != stockMovement?.id) {
                command.errors.reject("Row ${index + 1}: Order number ${params.identifier}: Cannot include line items from other outbound orders (${params.identifier}) when editing an order (${targetStockMovement.identifier})")
            }
        }

        if (stockMovement && stockMovement?.stockMovementStatusCode > StockMovementStatusCode.CREATED) {
            command.errors.reject("Row ${index + 1}: Order number ${params.identifier}: Cannot edit outbound order with identifier ${params.identifier} with status ${stockMovement?.status}")
        }

        if (!params?.origin) {
            command.errors.reject("Row ${index + 1}: Order number ${params.identifier} failed: Origin is required")
        }
        if (!params?.destination) {
            command.errors.reject("Row ${index + 1}: Order number ${params.identifier} failed: Destination is required")
        }
        if (params?.quantity < 0) {
            command.errors.reject("Row ${index + 1}: Order number ${params.identifier} failed: Requested Quantity is required")
        }
        if(command?.location?.locationNumber != params.origin) {
            command.errors.reject("Row ${index + 1}: Order number ${params.identifier} failed: Origin (${params.origin}) must match the current location (${command?.location?.locationNumber})")
        }

        Location origin = Location.findByLocationNumber(params.origin)
        if (!origin) {
            command.errors.reject("Row ${index + 1}: Order number ${params.identifier} failed: Unknown Supplier [${params.origin}]")
        }

        Location destination = Location.findByLocationNumber(params.destination)
        if (!destination) {
            command.errors.reject("Row ${index + 1}: Order number ${params.identifier} failed: Unknown Destination [${params.destination}]")
        }


        Integer daysRequiredBeforeDeliveryDate = grailsApplication.config.openboxes.integration.daysRequiredBeforeDeliveryDate
        def requestedDeliveryDate = new SimpleDateFormat("yyyy-MM-dd").parse(params.deliveryDate.toString())
        if (!requestedDeliveryDate || requestedDeliveryDate - new Date() < daysRequiredBeforeDeliveryDate) {
            String message = "Row ${index + 1}: Order number ${params.identifier} failed: Delivery date [${params.deliveryDate}] must occur at least seven days from today"
            //notificationService.sendRequisitionRejectedNotifications(params.identifier, message, [RoleType.ROLE_SHIPMENT_NOTIFICATION])
            command.errors.reject(message)
        }

        // Instantiate stock movement if it does not exist
        if (!stockMovement) {
            stockMovement = new StockMovement()
        }
        stockMovement.identifier = params.identifier
        stockMovement.description = "Outbound Order ${params.identifier}"
        stockMovement.stockMovementStatusCode = StockMovementStatusCode.CREATED
        stockMovement.stockMovementType = StockMovementType.OUTBOUND
        stockMovement.requestType = RequisitionType.DEFAULT
        stockMovement.sourceType = RequisitionSourceType.PAPER
        stockMovement.origin = origin
        stockMovement.destination = destination
        stockMovement.dateRequested = new Date()
        stockMovement.requestedBy = AuthService.currentUser.get()
        stockMovement.requestedDeliveryDate = requestedDeliveryDate
        stockMovement.expectedShippingDate = requestedDeliveryDate
        stockMovement.expectedDeliveryDate = requestedDeliveryDate
        return stockMovement
    }

    StockMovementItem buildStockMovementItem(ImportDataCommand command, Map params, Integer index) {
        log.info "Build stock movement item " + params

        String productCode = params.productCode
        Product product = Product.findByProductCode(productCode)
        if(!product) {
            command.errors.reject("Row ${index + 1}: Order number ${params.identifier} failed: Unknown SKU [${productCode}]")
        }

        def quantityRequested = params.quantity as Integer
        if (quantityRequested < 0) {
            command.errors.reject("Row ${index + 1}: Order number ${params.identifier} failed: Requested quantity [${quantityRequested}] for SKU ${productCode} should be greater than or equal to 0")
        }

        StockMovementItem stockMovementItem = new StockMovementItem()
        stockMovementItem.product = product
        stockMovementItem.quantityRequested = quantityRequested
        stockMovementItem.comments = params.specialInstructions
        return stockMovementItem
    }
}
