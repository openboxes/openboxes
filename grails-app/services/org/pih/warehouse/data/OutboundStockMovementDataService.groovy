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
import org.pih.warehouse.core.Location
import org.pih.warehouse.importer.ImportDataCommand
import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import org.pih.warehouse.requisition.RequisitionStatus
import org.pih.warehouse.requisition.RequisitionType
import org.springframework.validation.BeanPropertyBindingResult
import org.pih.warehouse.auth.AuthService

class OutboundStockMovementDataService {

    def stockMovementService
    def tmsIntegrationService

    Boolean validateData(ImportDataCommand command) {
        log.info "Validate data " + command.filename
        command.data.eachWithIndex { params, index ->

            if (!params?.origin) {
                throw new IllegalArgumentException("Row ${index + 1}: Origin is required")
            }
            if (!params?.destination) {
                throw new IllegalArgumentException("Row ${index + 1}: Destination is required")
            }
            if (params?.quantity < 0) {
                throw new IllegalArgumentException("Row ${index + 1}: Requested quantity (${params.quantity}) is required")
            }
            if(command?.location?.locationNumber != params.origin) {
                throw new IllegalArgumentException("Row ${index + 1}: Origin location (${params.origin}) must match the current location (${command?.location?.locationNumber})")
            }

            // validate requisition
            Requisition requisition = Requisition.findByRequestNumber(params.requestNumber)
            if (command.id) {

                if (!requisition) {
                    throw new IllegalArgumentException("Row ${index + 1}: Cannot locate outbound order with identifier ${params.requestNumber}")
                }

                Requisition targetRequisition = Requisition.get(command.id)
                if (!requisition || command.id != requisition.id) {
                    throw new IllegalArgumentException("Row ${index + 1}: Cannot include line items from other outbound orders (${requisition.requestNumber}) when editing an order (${targetRequisition.requestNumber})")
                }
            }

            if (requisition.status >= RequisitionStatus.EDITING) {
                throw new IllegalArgumentException("Row ${index + 1}: Cannot edit outbound order with identifier ${requisition.requestNumber} with status ${requisition?.status}")
            }


            RequisitionItem requisitionItem = buildRequisitionItem(params)
            if (!requisitionItem.validate()) {
                requisitionItem.errors.each { BeanPropertyBindingResult error ->
                    command.errors.reject("${index + 1}: ${requisitionItem} ${error.getFieldError()}")
                }
            }
        }
    }

    void importData(ImportDataCommand command) {
        log.info "Import data " + command.filename
        Set<String> identifiers = new HashSet<String>()
        command.data.eachWithIndex {params, index ->
            RequisitionItem requisitionItem = buildRequisitionItem(params)
            if(requisitionItem.validate()){

                // Keep track of all requisition updated
                identifiers.add(requisitionItem.requisition.requestNumber)

                // Delete requisition item with quantity = 0
                if (!requisitionItem.quantity) {
                    Requisition requisition = requisitionItem.requisition
                    requisition.removeFromRequisitionItems(requisitionItem)
                    requisitionItem.delete()
                    requisition.save()
                }
                // otherwise update requisition item
                else {
                    requisitionItem.save(failOnError: true)
                }
            }
        }

        // Generate hypothetical delivery order for newly created stock movements
        if (identifiers) {
            identifiers.each { String identifier ->
                StockMovement stockMovement = stockMovementService.getStockMovementByIdentifier(identifier, Boolean.FALSE)
                tmsIntegrationService.uploadDeliveryOrder(stockMovement)
            }
        }
    }


    RequisitionItem buildRequisitionItem(Map params) {
        String productCode = params.productCode
        Product product = Product.findByProductCode(productCode)
        if(!product) {
            throw new IllegalArgumentException("Product not found for ${productCode}")
        }

        def quantityRequested = params.quantity as Integer
        if (quantityRequested < 0) {
            throw new IllegalArgumentException("Requested quantity should be greater than or equal to 0")
        }

        def deliveryDate = params.deliveryDate
        if (!isDateOneWeekFromNow(deliveryDate)) {
            throw new IllegalArgumentException("Delivery date must be after seven days from now")
        }

        def requestNumber = params.requestNumber
        def requisition = Requisition.findByRequestNumber(requestNumber)
        if (!requisition) {
            String name = "Outbound Order ${requestNumber}"
            requisition = new Requisition(
                    name: name,
                    requestNumber: requestNumber,
                    dateRequested: new Date(),
                    type: RequisitionType.DEFAULT,
                    status: RequisitionStatus.CREATED,
                    requestedBy: AuthService.currentUser.get()
            )
        }
        requisition.origin = findLocationByLocationNumber(params.origin)
        requisition.destination = findLocationByLocationNumber(params.destination)
        requisition.requestedDeliveryDate = deliveryDate.toDate()
        requisition.save(failOnError: true)

        def requisitionItem = RequisitionItem.createCriteria().get {
            eq 'product' , product
            eq "requisition", requisition
        }
        if (!requisitionItem) {
            requisitionItem = new RequisitionItem()
        }

        requisitionItem.product = product
        requisitionItem.quantity = quantityRequested
        requisitionItem.description = params.description

        requisition.addToRequisitionItems(requisitionItem)

        return requisitionItem
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
