/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/
package org.pih.warehouse.order

import grails.databinding.BindUsing
import grails.util.Holders
import org.pih.warehouse.EmptyStringsToNullBinder
import org.pih.warehouse.core.BudgetCode
import org.pih.warehouse.core.GlAccount
import org.pih.warehouse.invoice.OrderAdjustmentInvoiceStatus
import org.pih.warehouse.invoice.InvoiceItem
import org.pih.warehouse.invoice.InvoiceTypeCode

class OrderAdjustment implements Serializable, Comparable<OrderAdjustment> {

    def publishRefreshEvent() {
        if (order?.isPurchaseOrder && !disableRefresh) {
            Holders.grailsApplication.mainContext.publishEvent(new RefreshOrderSummaryEvent(order))
        }
    }

    def afterUpdate() {
        publishRefreshEvent()
    }

    String id

    @BindUsing({ obj, source -> EmptyStringsToNullBinder.bindEmptyStringToNull(source, "amount")})
    BigDecimal amount

    @BindUsing({ obj, source -> EmptyStringsToNullBinder.bindEmptyStringToNull(source, "percentage")})
    BigDecimal percentage

    String description      // overrides description of order adjustment type

    String comments

    OrderAdjustmentType orderAdjustmentType

    BudgetCode budgetCode

    GlAccount glAccount

    // Audit fields
    Date dateCreated
    Date lastUpdated

    Boolean canceled = Boolean.FALSE

    Boolean disableRefresh = Boolean.TRUE

    static transients = [
        'totalAdjustments',
        'postedPurchaseInvoiceItems',
        "invoices",
        "hasInvoices",
        "hasPrepaymentInvoice",
        "hasRegularInvoice",
        "disableRefresh",
    ]

    static belongsTo = [order: Order, orderItem: OrderItem]

    static hasMany = [invoiceItems: InvoiceItem]

    static mapping = {
        id generator: 'uuid'
        invoiceItems joinTable: [name: 'order_adjustment_invoice', key: 'order_adjustment_id']
    }

    static constraints = {
        order(nullable:false)
        orderItem(nullable:true)
        orderAdjustmentType(nullable:true)
        amount(nullable:true)
        percentage(nullable:true)
        description(nullable:false, blank: false)
        comments(nullable: true)
        budgetCode(nullable: true)
        glAccount(nullable: true)
        canceled(nullable: true)
    }


    def getTotalAdjustments() {
        return amount ?: percentage ? orderItem ? orderItem?.subtotal * (percentage/100) : order.subtotal * (percentage/100) : 0
    }

    def getTotalAmountNotInvoiced() {
        return totalAdjustments - unitPriceOnPostedInvoices
    }

    def getPostedPurchaseInvoiceItems() {
        invoiceItems?.findAll {
            it.invoice.datePosted != null && it.invoice.isRegularInvoice
        }
    }

    Boolean getIsFullyInvoiced() {
        return Math.abs(unitPriceOnPostedInvoices) >= Math.abs(totalAdjustments)
    }

    OrderAdjustmentInvoiceStatus getDerivedPaymentStatus() {
        // An invoiced or partially invoiced adjustment will always have posted invoice items
        if (postedPurchaseInvoiceItems.empty) {
            return OrderAdjustmentInvoiceStatus.NOT_INVOICED
        }

        // check if order adjustment was invoiced as canceled
        // either with canceled flag or amount 0
        // then mark this order adjustment as invoiced
        if (canceled || totalAdjustments == 0) {
            return OrderAdjustmentInvoiceStatus.INVOICED
        }

        // if order adjustment was already invoiced but with amount 0
        // meaning it was invoiced as canceled item
        if (unitPriceOnPostedInvoices == 0) {
            return OrderAdjustmentInvoiceStatus.NOT_INVOICED
        }

        if (isFullyInvoiced) {
            return OrderAdjustmentInvoiceStatus.INVOICED
        }

        return OrderAdjustmentInvoiceStatus.PARTIALLY_INVOICED
    }

    /**
     * Overall invoiced unit price for this adjustment. Can be positive, negative or 0.
     * */
    BigDecimal getInvoicedUnitPrice() {
        return invoiceItems?.sum {
            if (it.invoice?.isRegularInvoice && !it.inverse) {
                return it.unitPrice
            }
            return 0
        } ?: 0
    }

    /**
     * Posted invoiced unit price for this adjustment. Can be positive, negative or 0.
     * */
    BigDecimal getUnitPriceOnPostedInvoices() {
        return invoiceItems?.sum {
            if (it.invoice?.datePosted != null && it.invoice?.isRegularInvoice && !it.inverse) {
                return it.unitPrice
            }
            return 0
        } ?: 0
    }

    /**
     * Overall inversed unit price for this adjustment. Can be positive, negative or 0.
     * */
    BigDecimal getInversedUnitPrice() {
        return invoiceItems?.sum {
            if (it.inverse) {
                return it.unitPrice
            }
            return 0
        } ?: 0
    }

    def getInvoices() {
        return invoiceItems*.invoice.unique()
    }

    /**
     * Adjustment can be on a regular invoice if it is not canceled or if it is canceled, but has prepayment
     * */
    Boolean canBeOnRegularInvoice() {
        if (canceled) {
            return hasPrepaymentInvoice
        }

        return true
    }

    /**
     * Adjustment is invoiceable on regular invoice if:
     *  - adjustment is canceled, it has prepayment, does not have full unit price (total adjustment) invoiced in all regular invoices and order is placed
     *  - adjustment is not canceled, and does not have full unit price (total adjustment) invoiced in all regular invoices
     *  - adjustment does not have full unit price (total adjustment) invoiced in all regular invoices yet and order is placed
     * */
    Boolean isInvoiceable() {
        Boolean fullyInvoiced = invoicedUnitPrice == totalAdjustments

        if (canceled) {
            return hasPrepaymentInvoice && !hasRegularInvoice && order.placed && !fullyInvoiced
        }

        // handle cases when trying to invoice adjustment with amount 0
        if (totalAdjustments == 0) {
            return order.placed && !hasRegularInvoice
        }

        return order.placed && !fullyInvoiced
    }

    Boolean getHasInvoices() {
        return !invoices.empty
    }

    Boolean getHasPrepaymentInvoice() {
        return invoices.any { it.invoiceType?.code == InvoiceTypeCode.PREPAYMENT_INVOICE }
    }

    Boolean getHasRegularInvoice() {
        return invoices.any { it.invoiceType == null || it.invoiceType?.code == InvoiceTypeCode.INVOICE }
    }

    BigDecimal getUnitPriceAvailableToInvoice() {
        if (totalAdjustments == 0 || canceled) {
            return 0
        }

        return totalAdjustments - invoicedUnitPrice
    }

    @Override
    int compareTo(OrderAdjustment o) {
        return dateCreated <=> o.dateCreated ?:
                lastUpdated <=> o.lastUpdated ?:
                        id <=> o.id
    }
}
