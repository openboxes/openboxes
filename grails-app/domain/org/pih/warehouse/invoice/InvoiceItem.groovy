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

import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.pih.warehouse.auth.AuthService
import org.pih.warehouse.core.BudgetCode
import org.pih.warehouse.core.GlAccount
import org.pih.warehouse.core.UnitOfMeasure
import org.pih.warehouse.core.User
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderAdjustment
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.product.Product
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem

class InvoiceItem implements Serializable {

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

    Product product
    GlAccount glAccount
    BudgetCode budgetCode

    Integer quantity
    UnitOfMeasure quantityUom
    BigDecimal quantityPerUom = 1
    BigDecimal amount
    BigDecimal unitPrice

    // Audit fields
    Date dateCreated
    Date lastUpdated
    User createdBy
    User updatedBy

    static belongsTo = [invoice: Invoice]

    static hasMany = [shipmentItems: ShipmentItem, orderItems: OrderItem, orderAdjustments: OrderAdjustment]

    static mapping = {
        id generator: 'uuid'
        orderItems joinTable: [name: 'order_invoice', key: 'invoice_item_id', column: 'order_item_id']
        shipmentItems joinTable: [name: 'shipment_invoice', key: 'invoice_item_id', column: 'shipment_item_id']
        orderAdjustments joinTable: [name: 'order_adjustment_invoice', key: 'invoice_item_id', column: 'order_adjustment_id']
    }

    static transients = [
        'orderItem',
        'shipmentItem',
        'orderAdjustment',
        'shipment',
        'order',
        'description',
        'totalAmount',
        'unitOfMeasure',
        'isPrepaymentInvoice',
        'totalPrepaymentAmount'
    ]

    static constraints = {
        invoice(nullable: false)
        product(nullable: true)
        glAccount(nullable: true)
        budgetCode(nullable: true)

        quantity(nullable: false, min: 1)
        quantityUom(nullable: true)
        quantityPerUom(nullable: false)
        amount(nullable: true)
        unitPrice(nullable: true)

        updatedBy(nullable: true)
        createdBy(nullable: true)
    }

    OrderItem getOrderItem() {
        return orderItems ? orderItems?.find { it } : null
    }

    ShipmentItem getShipmentItem() {
        return shipmentItems ? shipmentItems?.find { it } : null
    }

    OrderAdjustment getOrderAdjustment() {
        return orderAdjustments ? orderAdjustments?.find { it } : null
    }

    Shipment getShipment() {
        return shipmentItem?.shipment
    }

    Order getOrder() {
        if (orderItem) {
            return orderItem.order
        }
        if (orderAdjustment) {
            return orderAdjustment.order
        }
        return shipmentItem?.orderItems?.find { it }?.order
    }

    String getDescription() {
        return orderAdjustment ? orderAdjustment.description : product?.name
    }

    // Total shipment item value
    def getTotalAmount() {
        def total = (quantity ?: 0.0) * (unitPrice ?: 0.0)
        if (isPrepaymentInvoice) {
            return total * ((order.paymentTerm?.prepaymentPercent?:100) / 100)
        }

        return total
    }

    def getTotalPrepaymentAmount() {
        return isPrepaymentInvoice ? totalAmount * (-1) : 0.0
    }

    String getUnitOfMeasure() {
        if (quantityUom) {
            return "${quantityUom?.code}/${quantityPerUom as Integer}"
        }
        else {
            def g = ApplicationHolder.application.mainContext.getBean( 'org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib' )
            return "${g.message(code:'default.ea.label').toUpperCase()}/1"
        }
    }

    boolean getIsPrepaymentInvoice() {
        return invoice.isPrepaymentInvoice
    }

    Map toJson() {
        return [
                id: id,
                orderNumber: order?.orderNumber,
                orderId: order?.id,
                shipmentNumber: shipment?.shipmentNumber,
                shipmentId: shipment?.id,
                budgetCode: budgetCode?.code,
                glCode: glAccount?.code,
                productCode: product?.productCode,
                description: description,
                quantity: quantity,
                uom: unitOfMeasure,
                amount: amount,
                unitPrice: unitPrice,
                totalAmount: totalAmount,
                totalPrepaymentAmount: totalPrepaymentAmount,
        ]
    }
}
