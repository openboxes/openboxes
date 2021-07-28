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

import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.pih.warehouse.core.BudgetCode
import org.pih.warehouse.core.GlAccount
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.UnitOfMeasure
import org.pih.warehouse.core.User
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.invoice.InvoiceItem
import org.pih.warehouse.invoice.InvoiceType
import org.pih.warehouse.invoice.InvoiceTypeCode
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductPackage
import org.pih.warehouse.product.ProductSupplier
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem
import org.pih.warehouse.shipping.ShipmentStatusCode

import java.text.DecimalFormat

class OrderItem implements Serializable, Comparable<OrderItem> {

    String id
    String description
    Category category
    Product product
    InventoryItem inventoryItem
    Integer quantity
    UnitOfMeasure quantityUom
    BigDecimal quantityPerUom = 1

    BigDecimal unitPrice
    String currencyCode
    ProductSupplier productSupplier
    ProductPackage productPackage

    User requestedBy    // the person who actually requested the item
    Person recipient

    OrderItemStatusCode orderItemStatusCode = OrderItemStatusCode.PENDING

    // Transfer order
    Location originBinLocation
    Location destinationBinLocation

    Date estimatedReadyDate
    Date estimatedShipDate
    Date estimatedDeliveryDate

    Date actualReadyDate
    Date actualShipDate
    Date actualDeliveryDate

    BudgetCode budgetCode

    GlAccount glAccount

    Integer orderIndex = 0

    // Audit fields
    Date dateCreated
    Date lastUpdated

    static mapping = {
        id generator: 'uuid'
        shipmentItems joinTable: [name: 'order_shipment', key: 'order_item_id']
    }

    static transients = [
            "orderItemType",
            "quantityInStandardUom",
            "quantityRemaining",
            "quantityReceived",
            "quantityReceivedInStandardUom",
            "quantityShipped",
            "quantityShippedInStandardUom",
            "quantityInShipments",
            "quantityInShipmentsInStandardUom",
            "total",
            "pendingShipmentItems",
            "shippedShipmentItems",
            "subtotal",
            "totalAdjustments",
            "unitOfMeasure",
            "invoices",
            "hasInvoices",
            "hasPrepaymentInvoice",
            "hasRegularInvoice",
            // Statuses
            "partiallyFulfilled",
            "completelyFulfilled",
            "completelyReceived",
            "pending",
            "quantityRemainingToShip",
            "invoiceItems",
            "quantityInvoiced",
            "quantityInvoicedInStandardUom"
    ]

    static belongsTo = [order: Order, parentOrderItem: OrderItem]

    static hasMany = [orderItems: OrderItem, shipmentItems: ShipmentItem, orderAdjustments: OrderAdjustment]

    static constraints = {
        description(nullable: true)
        category(nullable: true)
        product(nullable: false)
        inventoryItem(nullable: true)
        requestedBy(nullable: true)
        quantity(nullable: false, min: 1)
        quantityUom(nullable: true)
        quantityPerUom(nullable: false)
        productPackage(nullable: true)
        unitPrice(nullable: true)
        orderItemStatusCode(nullable: true)
        parentOrderItem(nullable: true)
        originBinLocation(nullable: true)
        destinationBinLocation(nullable: true)
        recipient(nullable: true)
        currencyCode(nullable: true)
        productSupplier(nullable: true)
        estimatedReadyDate(nullable: true)
        estimatedShipDate(nullable: true)
        estimatedDeliveryDate(nullable: true)
        actualReadyDate(nullable: true)
        actualShipDate(nullable: true)
        actualDeliveryDate(nullable: true)
        budgetCode(nullable: true)
        glAccount(nullable: true)
        orderIndex(nullable: true)
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

    def hasShipmentAssociated() {
        return shipmentItems ? shipmentItems.size() > 0 : false
    }

    def getPendingShipmentItems() {
        return order.pendingShipments*.shipmentItems*.findAll { it.orderItemId == this.id }?.flatten()?.toArray()
    }

    def getShippedShipmentItems() {
        return shipmentItems.findAll { it.shipment?.currentStatus >= ShipmentStatusCode.SHIPPED }
    }

    def hasShippedItems() {
        return shippedShipmentItems?shippedShipmentItems.size()>0:false
    }

    void refreshPendingShipmentItemRecipients() {
        pendingShipmentItems.each { ShipmentItem shipmentItem ->
            shipmentItem.recipient = recipient
        }
    }

    Integer getQuantityInStandardUom() {
        return quantity * quantityPerUom
    }

    Integer getQuantityShippedInStandardUom() {
        return shippedShipmentItems?.sum { ShipmentItem shipmentItem ->
            shipmentItem?.quantity
        }?:0
    }

    Integer getQuantityInShipmentsInStandardUom() {
        return shipmentItems?.sum { ShipmentItem shipmentItem ->
            shipmentItem?.quantity
        }?:0
    }

    Integer getQuantityReceivedInStandardUom() {
        return shippedShipmentItems?.sum { ShipmentItem shipmentItem ->
            shipmentItem?.quantityReceived
        }?:0
    }

    Integer getQuantityShipped() {
        return quantityShippedInStandardUom / quantityPerUom
    }

    Integer getQuantityReceived() {
        return quantityReceivedInStandardUom / quantityPerUom
    }

    Integer getQuantityInShipments() {
        return quantityInShipmentsInStandardUom / quantityPerUom
    }

    String getOrderItemType() {
        return "Product"
    }

    Integer getQuantityRemainingToShip(Shipment shipment) {
        def quantityInOtherShipments = shipmentItems?.findAll { it.shipment != shipment}?.sum { ShipmentItem shipmentItem ->
            shipmentItem?.quantity
        }
        def quantityRemaining = quantityInOtherShipments ? (quantity * quantityPerUom) - quantityInOtherShipments : (quantity * quantityPerUom)
        return quantityRemaining > 0 ? quantityRemaining : 0
    }

    Integer getQuantityRemaining() {
        def quantityRemaining = quantity - quantityShipped
        return quantityRemaining > 0 ? quantityRemaining : 0
    }

    // quantityAvailable for combined shipments
    def getQuantityRemainingToShip() {
        def quantityRemaining = quantity - quantityInShipments
        return quantityRemaining > 0 ? quantityRemaining : 0
    }

    Boolean isPartiallyFulfilled() {
        return quantityShipped > 0 && quantityShipped < quantity
    }

    Boolean isCompletelyFulfilled() {
        return quantityShipped >= quantity
    }

    Boolean isCompletelyReceived() {
        return quantityReceived >= quantity
    }

    Boolean isPending() {
        return !isCompletelyFulfilled()
    }

    /**
     * Gets all shipment items related to this order item
     * (ignoring any orphaned shipment item references)
     *
     * @return
     */
    def shipmentItems() {
        return shipmentItems
    }

    /**
     * Gets all shipments related to this order item
     * (ignoring any orphaned shipment item references)
     *
     * @return
     */
    def listShipments() {
        return shipmentItems*.shipment
    }

    def totalPrice() {
        return total
    }

    def getTotalAdjustments() {
        return orderAdjustments?.findAll {!it.canceled }.sum {
            return it.amount ?: it.percentage ? (it.percentage/100) * subtotal : 0
        }?:0
    }

    def getSubtotal() {
        return (quantity ?: 0.0) * (unitPrice ?: 0.0)
    }

    def getTotal() {
        return (subtotal + totalAdjustments)?:0
    }

    String toString() {
        return product?.name
    }

    int compareTo(OrderItem orderItem) {
        def sortOrder =
                dateCreated <=> orderItem?.dateCreated ?:
                        product?.name <=> orderItem?.product?.name ?:
                                quantity <=> orderItem?.quantity ?:
                                        id <=> orderItem?.id
        return sortOrder
    }

    List<InvoiceItem> getInvoiceItems() {
        return InvoiceItem.executeQuery("""
          SELECT ii
            FROM InvoiceItem ii
            LEFT JOIN ii.orderItems oi
            LEFT JOIN ii.shipmentItems si
            LEFT JOIN si.orderItems soi
            WHERE oi.id = :id OR soi.id = :id
          """, [id: id])
    }

    Integer getQuantityInvoicedInStandardUom() {
        return InvoiceItem.executeQuery("""
          SELECT SUM(ii.quantity)
            FROM InvoiceItem ii
            JOIN ii.invoice i
            JOIN ii.shipmentItems si
            JOIN si.orderItems oi
            WHERE oi.id = :id 
            AND i.datePosted IS NOT NULL
          """, [id: id])?.first() ?: 0
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

    Integer getQuantityInvoiced() {
        return quantityInvoicedInStandardUom / quantityPerUom
    }

    Map toJson() {
        return [
                id           : id,
                product      : product,
                quantity     : quantity,
                shipmentItems: shipmentItems,
        ]
    }

    Map toImport() {
        return [
                id : id,
                productCode : product?.productCode,
                sourceName : productSupplier?.name ?: "",
                supplierCode: productSupplier?.supplierCode ?: "",
                manufacturer : productSupplier?.manufacturer?.name ?: "",
                manufacturerCode : productSupplier?.manufacturerCode ?: "",
                quantity : quantity.toString(),
                unitPrice : unitPrice.toString(),
                unitOfMeasure: unitOfMeasure ?: "",
                budgetCode: budgetCode?.code ?: "",
        ]
    }



}
