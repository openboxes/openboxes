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
import org.pih.warehouse.core.Location
import org.pih.warehouse.importer.ImportDataCommand
import org.pih.warehouse.product.Product
import org.pih.warehouse.requisition.Requisition
import org.pih.warehouse.requisition.RequisitionItem
import org.springframework.validation.BeanPropertyBindingResult
import org.pih.warehouse.auth.AuthService

class OutboundStockMovementDataService {

    Boolean validateData(ImportDataCommand command) {
        log.info "Validate data " + command.filename
        command.data.eachWithIndex { params, index ->
            if (!params?.origin) {
                throw new IllegalArgumentException("Row ${index + 1}: Origin/Source is required")
            }
            if (!params?.destination) {
                throw new IllegalArgumentException("Row ${index + 1}: Destination is required")
            }
            if (!params?.quantity) {
                throw new IllegalArgumentException("Row ${index + 1}: Requested Quantity is required")
            }
            Requisition requisition = createOrUpdateRequisition(params)
            if (!requisition.validate()) {
                requisition.errors.each { BeanPropertyBindingResult error ->
                    command.errors.reject("${index + 1}: ${requisition.name} ${error.getFieldError()}")
                }
            }
        }
    }

    void importData(ImportDataCommand command) {
        log.info "Import data " + command.filename
        command.data.eachWithIndex {params, index ->
            Requisition requisition = createOrUpdateRequisition(params)
            if(requisition.validate()){
                requisition.save(failOnError: true)
            }
        }
    }

    Requisition createOrUpdateRequisition(Map params ){
        Product product = Product.findByProductCode(params.productCode)
        if(!product) {
            throw new IllegalArgumentException("Product not found for SKU Code")
        }
        def requisitionItem = RequisitionItem.createCriteria().get {
            eq 'product' , product
        }
        if(!requisitionItem) {
            requisitionItem = new RequisitionItem()
        }
        def requestedQuantity = params.quantity
        if (!(requestedQuantity > 0)) {
            throw new IllegalArgumentException("Requested quantity should be greater than 0")
        }
        requisitionItem.product = product
        requisitionItem.quantity = requestedQuantity
        def requestNumber = params.requestNumber
        def requisition = Requisition.findByRequestNumber(requestNumber)
        if (!requisition) {
            requisition = new Requisition(
                    name: 'Load Code',
                    requestNumber: requestNumber
            )
        }
        def deliveryDate = params.requestedDeliveryDate
        if (!isDateOneWeekFromNow(deliveryDate)) {
            throw new IllegalArgumentException("Delivery date must be after seven days from now")
        }
        requisition.requestedDeliveryDate = deliveryDate.toDate()
        Location origin = Location.findByLocationNumber(params.origin)
        if(!origin) {
            throw new IllegalArgumentException("Location not found for source.")
        }
        requisition.origin = origin
        Location destination = Location.findByLocationNumber(params.destination)
        if(!destination) {
            throw new IllegalArgumentException("Location not found for destination")
        }
        requisition.destination = destination
        if(params.description) {
            requisitionItem.description = params.description
        }
        requisition.requestedBy = AuthService.currentUser.get()
        requisition.addToRequisitionItems(requisitionItem)
        return requisition
    }

    boolean isDateOneWeekFromNow(def date) {
        LocalDate today = LocalDate.now()
        LocalDate oneWeekFromNow = new LocalDate(today.getYear(), today.getMonthOfYear(), today.getDayOfMonth()+7)
        if(date > oneWeekFromNow) {
            return true
        }
        return false
    }
}
