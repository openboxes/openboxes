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

    // Audit fields
    Date dateCreated
    Date lastUpdated
    User createdBy
    User updatedBy

    static belongsTo = [invoice: Invoice]

    static hasMany = [shipmentItems: ShipmentItem, orderAdjustments: OrderAdjustment]

    static mapping = {
        id generator: 'uuid'
        shipmentItems joinTable: [name: 'shipment_invoice', key: 'invoice_item_id', column: 'shipment_item_id']
        orderAdjustments joinTable: [name: 'order_adjustment_invoice', key: 'invoice_item_id', column: 'order_adjustment_id']
    }

    static transients = [
        'shipmentItem',
        'shipmentNumber',
        'orderAdjustment',
        'orderNumber',
        'description',
        'order',
        'orderItem',
        'totalAdjustments',
        'unitPrice',
        'totalItemPrice',
        'totalAmount',
        'unitOfMeasure'
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

        updatedBy(nullable: true)
        createdBy(nullable: true)
    }

    ShipmentItem getShipmentItem() {
        return shipmentItems ? shipmentItems?.iterator()?.next() : null
    }

    String getShipmentNumber() {
        return shipmentItem?.shipment?.shipmentNumber
    }

    OrderAdjustment getOrderAdjustment() {
        return orderAdjustments ? orderAdjustments?.iterator()?.next() : null
    }

    String getOrderNumber() {
        return shipmentItem?.orderNumber ?: orderAdjustment?.order?.orderNumber ?: null
    }

    String getDescription() {
        return orderAdjustment ? orderAdjustment.description : product?.name
    }

    Order getOrder() {
        return orderNumber ? Order.findByOrderNumber(orderNumber) : null
    }

    OrderItem getOrderItem() {
        return shipmentItem?.orderItems?.iterator()?.next()
    }

    // Total order adjustment value
    def getTotalAdjustments() {
        return orderAdjustments?.findAll {!it.canceled }?.sum { it.getTotalAdjustments() } ?: 0
    }

    def getUnitPrice() {
        def unitPrice = 0.0
        if (shipmentItems) {
            unitPrice = orderItem?.unitPrice
        } else if (orderAdjustments) {
            unitPrice = totalAdjustments
        }

        return unitPrice ?: 0.0
    }

    // Total shipment item value
    def getTotalItemPrice() {
        return (quantity ?: 0.0) * (unitPrice ?: 0.0)
    }

    // Total adjustments value if order adjustment based or total item value if shipment item based
    def getTotalAmount() {
        return totalAdjustments ?: totalItemPrice
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

    Map toJson() {
        return [
                id: id,
                orderNumber: orderNumber,
                shipmentNumber: shipmentNumber,
                budgetCode: budgetCode?.code,
                glCode: glAccount?.code,
                productCode: product?.productCode,
                description: description,
                quantity: quantity,
                uom: unitOfMeasure,
                amount: amount,
                unitPrice: unitPrice,
                totalAmount: totalAmount,
        ]
    }
}
