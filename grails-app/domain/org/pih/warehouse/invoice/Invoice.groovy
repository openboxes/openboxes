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

import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Organization
import org.pih.warehouse.core.UnitOfMeasure
import org.pih.warehouse.core.UnitOfMeasureConversion
import org.pih.warehouse.core.User
import org.pih.warehouse.order.Order
import org.pih.warehouse.shipping.ReferenceNumber
import org.pih.warehouse.shipping.ReferenceNumberType
import org.pih.warehouse.shipping.Shipment

class Invoice implements Serializable {

    def beforeInsert = {
        def currentUser = AuthService.currentUser.get()
        if (currentUser) {
            createdBy = currentUser
            updatedBy = currentUser
        }
    }

    def beforeUpdate = {
        def currentUser = AuthService.currentUser.get()
        if (currentUser) {
            updatedBy = currentUser
        }
    }

    String id
    String invoiceNumber
    String name
    String description

    InvoiceType invoiceType

    Organization partyFrom // Party generating the invoice
    Organization party // Party responsible for paying

    // Date fields
    Date dateInvoiced
    Date dateSubmitted
    Date datePosted
    Date dateDue
    Date datePaid

    // Currency
    UnitOfMeasure currencyUom

    // Audit fields
    Date dateCreated
    Date lastUpdated
    User createdBy
    User updatedBy

    static hasMany = [referenceNumbers: ReferenceNumber, invoiceItems: InvoiceItem]

    static mapping = {
        id generator: 'uuid'
        referenceNumbers cascade: "all-delete-orphan"
        invoiceItems cascade: "all-delete-orphan"
    }

    static transients = [
            'vendorInvoiceNumber',
            'totalValue',
            'totalValueNormalized',
            'documents',
            'status',
            'hasPrepaymentInvoice',
            'totalPrepaymentValue',
            'isPrepaymentInvoice',
            'prepaymentInvoices',
            'prepaymentItems',
            'orders',
            'shipments'
    ]

    static constraints = {
        invoiceNumber(nullable: false, blank: false, unique: true, maxSize: 255)
        name(nullable: true, maxSize: 255)
        description(nullable: true, maxSize: 255)

        invoiceType(nullable: true)

        partyFrom(nullable: true)
        party(nullable: true)

        dateDue(nullable: true)
        dateInvoiced(nullable: true)
        dateSubmitted(nullable: true)
        datePosted(nullable: true)
        datePaid(nullable: true)

        currencyUom(nullable: true)

        updatedBy(nullable: true)
        createdBy(nullable: true)
    }

    ReferenceNumber getReferenceNumber(String id) {
        def referenceNumberType = ReferenceNumberType.findById(id)
        if (referenceNumberType) {
            for (referenceNumber in referenceNumbers) {
                if (referenceNumber.referenceNumberType == referenceNumberType) {
                    return referenceNumber
                }
            }
        }
        return null
    }

    ReferenceNumber getVendorInvoiceNumber() {
        return getReferenceNumber(Constants.VENDOR_INVOICE_NUMBER_TYPE_ID)
    }

    Float getTotalValue() {
        return invoiceItems?.collect { it?.totalAmount }?.sum() ?: 0
    }

    Float getTotalValueNormalized() {
        BigDecimal currentExchangeRate
        String defaultCurrencyCode = ConfigurationHolder.config.openboxes.locale.defaultCurrencyCode
        if (currencyUom?.code != defaultCurrencyCode) {
            currentExchangeRate = UnitOfMeasureConversion.conversionRateLookup(defaultCurrencyCode, currencyUom?.code).list()
        }
        return totalValue * (currentExchangeRate ?: 1.0)
    }

    def getDocuments() {
        def documents = []
        invoiceItems.each {invoiceItem ->
            if (invoiceItem?.order?.documents) {
                invoiceItem?.order?.documents?.each {document ->
                    if (!documents.find { it.id == document.id }) {
                        documents.add(document.toJson())
                    }
                }
            }
        }
        return documents
    }

    def getStatus() {
        if (datePaid) {
            return InvoiceStatus.PAID
        } else if (datePosted) {
            return InvoiceStatus.POSTED
        } else if (dateSubmitted) {
            return InvoiceStatus.SUBMITTED
        }
        return InvoiceStatus.PENDING
    }

    boolean getHasPrepaymentInvoice() {
        return invoiceItems?.any { it.order?.hasPrepaymentInvoice }
    }

    boolean getIsPrepaymentInvoice() {
        return invoiceType?.code == InvoiceTypeCode.PREPAYMENT_INVOICE
    }

    Float getTotalPrepaymentValue() {
        return isPrepaymentInvoice ? invoiceItems.sum { it.totalAmount } : 0
    }

    List<Order> getOrders() {
        List<Order> orders = []
        orders += invoiceItems*.orderItems?.order?.flatten()
        orders += invoiceItems*.orderAdjustments?.order?.flatten()
        orders += invoiceItems*.shipmentItems*.shipment?.orders?.flatten()
        return orders.unique()
    }

    List<Shipment> getShipments() {
        return invoiceItems*.shipmentItems?.shipment?.flatten()?.unique()
    }

    // Technically it should be only one Prepayment Invoice for one Order (and only one Order in orders for 'final' invoice)
    List<Invoice> getPrepaymentInvoices() {
        // Avoid returning prepayment invoice for PREPAYMENT_INVOICE (it is only for 'final' invoice)
        if (invoiceType?.code == InvoiceTypeCode.PREPAYMENT_INVOICE) {
            return []
        }
        return orders?.invoices?.flatten()?.findAll { it.invoiceType?.code == InvoiceTypeCode.PREPAYMENT_INVOICE }?.unique()
    }

    List<InvoiceItem> getPrepaymentItems() {
        // Avoid returning prepayment items for PREPAYMENT_INVOICE (these are only for 'final' invoice)
        if (invoiceType?.code == InvoiceTypeCode.PREPAYMENT_INVOICE) {
            return []
        }
        return prepaymentInvoices?.invoiceItems?.flatten()
    }

    Map toJson() {
        return [
            id: id,
            invoiceNumber: invoiceNumber,
            vendorInvoiceNumber: vendorInvoiceNumber?.identifier,
            name: name,
            description: description,
            partyFrom: Organization.get(partyFrom?.id),
            dateInvoiced: dateInvoiced.format("MM/dd/yyyy"),
            dateSubmitted: dateSubmitted,
            datePosted: datePosted,
            dateDue: dateDue,
            datePaid: datePaid,
            currencyUom: currencyUom,
            vendor: party?.id,
            vendorName: "${party?.code} ${party?.name}",
            totalCount: invoiceItems?.size() ?: 0,
            totalValue: totalValue,
            invoiceType: invoiceType?.code?.name(),
            hasPrepaymentInvoice: hasPrepaymentInvoice,
            isPrepaymentInvoice: isPrepaymentInvoice,
            documents: documents,
        ]
    }
}
