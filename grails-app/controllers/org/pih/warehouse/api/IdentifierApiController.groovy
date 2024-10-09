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

import org.pih.warehouse.core.LocationIdentifierService
import org.pih.warehouse.data.ProductSupplierIdentifierService
import org.pih.warehouse.inventory.TransactionIdentifierService
import org.pih.warehouse.invoice.InvoiceIdentifierService
import org.pih.warehouse.order.OrderIdentifierService
import org.pih.warehouse.order.PurchaseOrderIdentifierService
import org.pih.warehouse.product.ProductIdentifierService
import org.pih.warehouse.receiving.ReceiptIdentifierService
import org.pih.warehouse.requisition.RequisitionIdentifierService
import org.pih.warehouse.shipping.ShipmentIdentifierService

class IdentifierApiController extends NoopApiController {

    ProductIdentifierService productIdentifierService
    ShipmentIdentifierService shipmentIdentifierService
    InvoiceIdentifierService invoiceIdentifierService
    LocationIdentifierService locationIdentifierService
    ReceiptIdentifierService receiptIdentifierService
    ProductSupplierIdentifierService productSupplierIdentifierService
    OrderIdentifierService orderIdentifierService
    PurchaseOrderIdentifierService purchaseOrderIdentifierService
    RequisitionIdentifierService requisitionIdentifierService
    TransactionIdentifierService transactionIdentifierService

    def create() {
        log.debug "create " + params
        def identifierType = params.identifierType
        if (!identifierType && !params.identifierFormat) {
            throw new IllegalArgumentException("Must specify identifierType or identifierFormat as a parameter")
        }

        String identifier
        switch (identifierType) {
            case "product":
                identifier = productIdentifierService.generate()
                break
            case "productSupplier":
                identifier = productSupplierIdentifierService.generate()
                break
            case "shipment":
                identifier = shipmentIdentifierService.generate()
                break
            case "requisition":
                identifier = requisitionIdentifierService.generate()
                break
            case "order":
                identifier = orderIdentifierService.generate()
                break
            case "purchaseOrder":
                identifier = purchaseOrderIdentifierService.generate()
                break
            case "transaction":
                identifier = transactionIdentifierService.generate()
                break
            case "invoice":
                identifier = invoiceIdentifierService.generate()
                break
            case "location":
                identifier = locationIdentifierService.generate()
                break
            case "receipt":
                identifier = receiptIdentifierService.generate()
                break
            default:
                throw new IllegalArgumentException("Illegal identifier type ${identifierType}")
        }
        render([data: identifier] as JSON)
    }
}
