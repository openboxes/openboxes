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

import org.pih.warehouse.core.BudgetCode
import org.pih.warehouse.core.GlAccount
import org.pih.warehouse.invoice.InvoiceItem
import org.pih.warehouse.invoice.InvoiceType
import org.pih.warehouse.invoice.InvoiceTypeCode

class OrderAdjustment implements Serializable {

    String id
    BigDecimal amount
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

    static transients = [
        'totalAdjustments',
        'submittedInvoiceItems',
        'invoiceItems',
        'isInvoiced',
        "invoices",
        "hasInvoices",
        "hasPrepaymentInvoice",
        "hasRegularInvoice",
    ]

    static belongsTo = [order: Order, orderItem: OrderItem]

    static mapping = {
        id generator: 'uuid'
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

    def getInvoiceItems() {
        return InvoiceItem.executeQuery("""
          SELECT ii
            FROM InvoiceItem ii
            JOIN ii.invoice i
            JOIN ii.orderAdjustments oa
            WHERE oa.id = :id 
          """, [id: id])
    }

    def getSubmittedInvoiceItems() {
        return InvoiceItem.executeQuery("""
          SELECT ii
            FROM InvoiceItem ii
            JOIN ii.invoice i
            JOIN ii.orderAdjustments oa
            WHERE oa.id = :id 
            AND i.datePosted IS NOT NULL
          """, [id: id])
    }

    Boolean getIsInvoiced() {
        return !submittedInvoiceItems.empty
    }

    def getInvoices() {
        return invoiceItems*.invoice.unique()
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
}
