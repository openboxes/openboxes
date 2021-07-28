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
import org.pih.warehouse.core.Organization
import org.pih.warehouse.invoice.Invoice
import org.pih.warehouse.invoice.InvoiceItemCandidate
import org.pih.warehouse.invoice.InvoiceItem
import org.pih.warehouse.invoice.InvoiceType
import org.pih.warehouse.invoice.InvoiceTypeCode

class InvoiceApiController {

    def identifierService
    def invoiceService

    def list = {
        List invoices = invoiceService.getInvoices(params)
        render([data: invoices.collect { it.toJson() }] as JSON)
    }

    def read = {
        Invoice invoice = Invoice.get(params.id)
        if (!invoice) {
            throw new IllegalArgumentException("No Invoice found for invoice ID ${params.id}")
        }

        render([data: invoice?.toJson()] as JSON)
    }

    def create = { Invoice invoice ->
        JSONObject jsonObject = request.JSON

        Location currentLocation = Location.get(session.warehouse.id)
        if (!currentLocation) {
            throw new IllegalArgumentException("User must be logged into a location to create invoice")
        }

        bindInvoiceData(invoice, currentLocation, jsonObject)

        if (invoice.hasErrors() || !invoice.save(flush: true)) {
            throw new ValidationException("Invalid invoice", invoice.errors)
        }

        render([data: invoice?.toJson()] as JSON)
    }

    def update = {
        JSONObject jsonObject = request.JSON

        Invoice existingInvoice = Invoice.get(params.id)
        if (!existingInvoice) {
            throw new IllegalArgumentException("No Invoice found for invoice ID ${params.id}")
        }

        Location currentLocation = Location.get(session.warehouse.id)
        if (!currentLocation) {
            throw new IllegalArgumentException("User must be logged into a location to create invoice")
        }

        bindInvoiceData(existingInvoice, currentLocation, jsonObject)

        if (existingInvoice.hasErrors() || !existingInvoice.save(flush: true)) {
            throw new ValidationException("Invalid invoice", existingInvoice.errors)
        }

        render([data: existingInvoice?.toJson()] as JSON)
    }

    Invoice bindInvoiceData(Invoice invoice, Location currentLocation, JSONObject jsonObject) {
        bindData(invoice, jsonObject)

        if (!invoice.partyFrom) {
            invoice.partyFrom = currentLocation?.organization
        }

        if (!invoice.invoiceNumber) {
            invoice.invoiceNumber = identifierService.generateInvoiceIdentifier()
        }

        if (!invoice.invoiceType) {
            invoice.invoiceType = InvoiceType.findByCode(InvoiceTypeCode.INVOICE)
        }

        invoice.party = Organization.get(jsonObject?.vendor)

        // TODO: find or create vendor invoice number in reference numbers
        invoiceService.createOrUpdateVendorInvoiceNumber(invoice, jsonObject?.vendorInvoiceNumber)

        return invoice
    }

    def getInvoiceItems = {
        List<InvoiceItem> invoiceItems = invoiceService.getInvoiceItems(params.id, params.max, params.offset)
        render([data: invoiceItems, totalCount: invoiceItems.totalCount?:invoiceItems.size()] as JSON)
    }

    def getInvoiceItemCandidates = {
        List<InvoiceItemCandidate> invoiceItemCandidates = invoiceService.getInvoiceItemCandidates(
            params.id, params.orderNumbers, params.shipmentNumbers
        )
        render([data: invoiceItemCandidates] as JSON)
    }

    def getOrderNumbers = {
        List orderNumbers = invoiceService.getDistinctFieldFromInvoiceItemCandidates(params.id, "orderNumber")

        render([data: orderNumbers] as JSON)
    }

    def getShipmentNumbers = {
        List shipmentNumbers = invoiceService.getDistinctFieldFromInvoiceItemCandidates(params.id, "shipmentNumber")

        render([data: shipmentNumbers] as JSON)
    }

    def removeItem = {
        invoiceService.removeInvoiceItem(params.id)
        render status: 204
    }

    def updateItems = {
        JSONObject jsonObject = request.JSON

        Invoice invoice = Invoice.get(params.id)
        List invoiceItems = jsonObject.remove("invoiceItems")
        invoiceService.updateItems(invoice, invoiceItems)
        render status: 204
    }

    def submitInvoice = {
        Invoice invoice = Invoice.get(params.id)
        if (!invoice) {
            throw new IllegalArgumentException("No Invoice found for invoice ID ${params.id}")
        }
        invoiceService.submitInvoice(invoice)
        render([data: invoice?.toJson()] as JSON)
    }

    def postInvoice = {
        Invoice invoice = Invoice.get(params.id)
        if (!invoice) {
            throw new IllegalArgumentException("No Invoice found for invoice ID ${params.id}")
        }
        invoiceService.postInvoice(invoice)
        render([data: invoice?.toJson()] as JSON)
    }


    def getPrepaymentItems = {
        Invoice invoice = Invoice.get(params.id)
        List<InvoiceItem> prepaymentItems = invoice.prepaymentItems
        render([data: prepaymentItems, totalCount: prepaymentItems.size()] as JSON)
    }
}
