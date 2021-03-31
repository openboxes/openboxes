/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.invoice

import org.apache.commons.lang.StringUtils
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.shipping.ReferenceNumber
import org.pih.warehouse.shipping.ReferenceNumberType

class InvoiceService {

    boolean transactional = true

    def dataService
    def invoiceItemService

    def getInvoices(Invoice invoiceTemplate, Map params) {
        def invoices = Invoice.createCriteria().list(params) {
            and {
                if (invoiceTemplate.invoiceNumber) {
                    ilike("invoiceNumber", "%" + invoiceTemplate.invoiceNumber + "%")
                }
                if (invoiceTemplate.dateInvoiced) {
                    eq('dateInvoiced', invoiceTemplate.dateInvoiced)
                }
                if (invoiceTemplate.createdBy) {

                    eq('createdBy', invoiceTemplate.createdBy)
                }
            }
            order("dateInvoiced", "desc")
        }
        return invoices
    }

    def getInvoiceItems(String id, String max, String offset) {
        Invoice invoice = Invoice.get(id)

        if (!invoice) {
            return []
        }

        List <InvoiceItem> invoiceItems
        if (max != null && offset != null) {
            invoiceItems = InvoiceItem.createCriteria().list(max: max.toInteger(), offset: offset.toInteger()) {
                eq("invoice", invoice)
            }
        } else {
            invoiceItems = InvoiceItem.createCriteria().list() {
                eq("invoice", invoice)
            }
        }

        return invoiceItems
    }

    def getInvoiceCandidates(String id, String orderNumber, String shipmentNumber) {
        Invoice invoice = Invoice.get(id)

        if (!invoice) {
            return []
        }

        List<InvoiceCandidate> invoiceCandidates = InvoiceCandidate.createCriteria()
            .list() {
                if (invoice.party) {
                    eq("vendor", invoice.party)
                }

                if (invoice.currencyUom?.code) {
                    eq("currencyCode", invoice.currencyUom.code)
                }

                if (StringUtils.isNotBlank(orderNumber)) {
                    ilike("orderNumber", "%" + orderNumber + "%")
                }

                if (StringUtils.isNotBlank(shipmentNumber)) {
                    ilike("shipmentNumber", "%" + shipmentNumber + "%")
                }
            }

        return invoiceCandidates
    }

    def listInvoices(Location currentLocation, Map params) {
        String query = """
            select * 
            from invoice_list
            where party_from_id = :partyId
            AND created_by_id = IFNULL(:createdBy, created_by_id)
            AND invoice_number LIKE :invoiceNumber
            AND date_invoiced = IFNULL(:dateInvoiced, date_invoiced)
            order by date_invoiced, id
            """
        def data = dataService.executeQuery(query, [
                 partyId: currentLocation?.organization?.id,
                 createdBy: params.createdBy,
                 invoiceNumber: "%" + params.invoiceNumber + "%",
                 dateInvoiced: params.dateInvoiced
                ])

        def invoices = data.collect { invoice ->
            [
                id : invoice.id,
                invoiceNumber: invoice.invoice_number,
                itemCount: invoice.item_count,
                currency: invoice.currency,
                vendorInvoiceNumber: invoice.vendor_invoice_number,
                totalValue: invoice.total_value
            ]
        }
        return invoices
    }

    ReferenceNumber createOrUpdateVendorInvoiceNumber(Invoice invoice, String vendorInvoiceNumber) {
        ReferenceNumberType vendorInvoiceNumberType = ReferenceNumberType.findById(Constants.VENDOR_INVOICE_NUMBER_TYPE_ID)
        if (!vendorInvoiceNumberType) {
            throw new IllegalStateException("Must configure reference number type for Vendor Invoice Number with ID '${Constants.VENDOR_INVOICE_NUMBER_TYPE_ID}'")
        }

        ReferenceNumber referenceNumber = invoice.vendorInvoiceNumber

        if (vendorInvoiceNumber) {
            if (!referenceNumber) {
                referenceNumber = new ReferenceNumber()
                referenceNumber.identifier = vendorInvoiceNumber
                referenceNumber.referenceNumberType = vendorInvoiceNumberType
                invoice.addToReferenceNumbers(referenceNumber)
            }
            else {
                referenceNumber.identifier = vendorInvoiceNumber
            }
        }
        else if (referenceNumber) {
            invoice.removeFromReferenceNumbers(referenceNumber)
        }
        return referenceNumber
    }

    def removeInvoiceItem(String id) {
        InvoiceItem invoiceItem = InvoiceItem.get(id)
        Invoice invoice = invoiceItem.invoice
        invoice.removeFromInvoiceItems(invoiceItem)
        invoiceItem.delete()
    }

    def addItems(Invoice invoice, List items) {
        items.each { item ->
            InvoiceCandidate candidateItem = InvoiceCandidate.get(item.id)
            if (candidateItem) {
                InvoiceItem invoiceItem = invoiceItemService.createFromCandidate(candidateItem)
                invoiceItem.quantity = item.quantityToInvoice
                invoice.addToInvoiceItems(invoiceItem)
            }
        }
        invoice.save()
    }

    def updateItems(Invoice invoice, List items) {
        invoice.invoiceItems.each { invoiceItem ->
            def newItem = items.find { it.id == invoiceItem.id }
            invoiceItem.quantity = newItem.quantity
        }
        invoice.save()
    }
}
