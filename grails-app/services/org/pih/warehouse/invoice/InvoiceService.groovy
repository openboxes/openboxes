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

import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.UnitOfMeasure
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderAdjustment
import org.pih.warehouse.product.Product
import org.pih.warehouse.shipping.ReferenceNumber
import org.pih.warehouse.shipping.ReferenceNumberType
import org.pih.warehouse.shipping.ShipmentItem

class InvoiceService {

    boolean transactional = true

    def dataService
    def identifierService

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

    def getInvoiceItemCandidates(String id, List orderNumbers, List shipmentNumbers) {
        Invoice invoice = Invoice.get(id)

        if (!invoice) {
            return []
        }

        def currentLocation = AuthService?.currentLocation?.get()
        List<InvoiceItemCandidate> invoiceItemCandidates = InvoiceItemCandidate.createCriteria()
            .list() {
                if (invoice.party) {
                    eq("vendor", invoice.party)
                }

                if (invoice.currencyUom?.code) {
                    eq("currencyCode", invoice.currencyUom.code)
                }

                if (orderNumbers.size() > 0) {
                    'in'("orderNumber", orderNumbers)
                }

                if (shipmentNumbers.size() > 0) {
                    'in'("shipmentNumber", shipmentNumbers)
                }

                order {
                    eq("destinationParty", currentLocation.organization)
                }
            }

        return invoiceItemCandidates
    }

    def getDistinctFieldFromInvoiceItemCandidates(String id, String distinctField) {
        Invoice invoice = Invoice.get(id)

        if (!invoice) {
            return []
        }

        def currentLocation = AuthService?.currentLocation?.get()
        List<InvoiceItemCandidate> invoiceItemCandidates = InvoiceItemCandidate.createCriteria()
            .list() {
                projections {
                    groupProperty(distinctField)
                }
                if (invoice.party) {
                    eq("vendor", invoice.party)
                }

                if (invoice.currencyUom?.code) {
                    eq("currencyCode", invoice.currencyUom.code)
                }

                order {
                    eq("destinationParty", currentLocation.organization)
                }

                ne(distinctField, "")
            }

        return invoiceItemCandidates
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

    def updateItems(Invoice invoice, List items) {
        items.each { item ->
            InvoiceItem invoiceItem = InvoiceItem.get(item.id)
            if (invoiceItem) {
                invoiceItem.quantity = item.quantity
            } else {
                InvoiceItemCandidate candidateItem = InvoiceItemCandidate.get(item.id)
                if (!candidateItem) {
                    throw new IllegalArgumentException("No Invoice Item Candidate found with ID ${item.id}")
                }
                invoiceItem = createFromInvoiceItemCandidate(candidateItem)
                invoiceItem.quantity = item.quantityToInvoice
                invoice.addToInvoiceItems(invoiceItem)
            }
        }

        invoice.save()
    }

    InvoiceItem createFromInvoiceItemCandidate(InvoiceItemCandidate candidate) {
        InvoiceItem invoiceItem = new InvoiceItem(
            budgetCode: candidate.budgetCode,
            product: candidate.productCode ? Product.findByProductCode(candidate.productCode) : null,
            glAccount: candidate.glAccount,
            quantity: candidate.quantity,
            quantityUom: candidate.quantityUom,
            quantityPerUom: candidate.quantityPerUom ?: 1,
        )

        ShipmentItem shipmentItem = ShipmentItem.get(candidate.id)
        if (shipmentItem) {
            invoiceItem.addToShipmentItems(shipmentItem)
        } else {
            OrderAdjustment orderAdjustment = OrderAdjustment.get(candidate.id)
            if (orderAdjustment) {
                invoiceItem.addToOrderAdjustments(orderAdjustment)
            }
        }

        return invoiceItem
    }

    def submitInvoice(Invoice invoice) {
        invoice.dateSubmitted = new Date()
        invoice.save()
    }

    Invoice generatePrepaymentInvoice(Order order) {
        if (order.orderItems.any { it.hasInvoices } || order.orderAdjustments.any { it.hasInvoice }) {
            throw new Exception("Some order items or order adjustments for this order already have been invoiced")
        }

        Invoice invoice = new Invoice()
        invoice.invoiceNumber = identifierService.generateInvoiceIdentifier()
        invoice.name = order.name
        invoice.description = order.description
        invoice.partyFrom = order.destination.organization
        invoice.party = order.origin.organization
        invoice.dateInvoiced = new Date()
        invoice.currencyUom = UnitOfMeasure.findByCode(order.currencyCode)
        invoice.invoiceType = InvoiceType.findByCode(InvoiceTypeCode.PREPAYMENT_INVOICE)
        createOrUpdateVendorInvoiceNumber(invoice, order.orderNumber + Constants.PREPAYMENT_INVOICE_SUFFIX)

        List invoiceCandidates = InvoiceItemCandidate.findAllByOrderNumber(order.orderNumber)

        if (invoiceCandidates.size() == 0) {
            throw new Exception("No invoice item candidates found for given order")
        }

        invoiceCandidates.each { InvoiceItemCandidate candidateItem ->
            InvoiceItem invoiceItem = createFromInvoiceItemCandidate(candidateItem)
            invoice.addToInvoiceItems(invoiceItem)
        }

        return invoice.save()
    }
}
