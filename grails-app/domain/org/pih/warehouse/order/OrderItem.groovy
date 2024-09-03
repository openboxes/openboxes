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

import grails.util.Holders
import org.grails.plugins.web.taglib.ValidationTagLib
import org.pih.warehouse.core.BudgetCode
import org.pih.warehouse.core.GlAccount
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.UnitOfMeasure
import org.pih.warehouse.core.User
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.invoice.InvoiceItem
import org.pih.warehouse.invoice.InvoiceTypeCode
import org.pih.warehouse.picklist.PicklistItem
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductPackage
import org.pih.warehouse.product.ProductSupplier
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem
import org.pih.warehouse.shipping.ShipmentStatusCode

class OrderItem implements Serializable, Comparable<OrderItem> {

    def publishRefreshEvent() {
        if (order?.isPurchaseOrder && !disableRefresh) {
            Holders.grailsApplication.mainContext.publishEvent(new RefreshOrderSummaryEvent(order))
        }
    }

    def afterUpdate() {
        publishRefreshEvent()
    }

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

    Boolean disableRefresh = Boolean.TRUE

    static mapping = {
        id generator: 'uuid'
        shipmentItems joinTable: [name: 'order_shipment', key: 'order_item_id']
        picklistItems cascade: "all-delete-orphan", sort: "id"
        invoiceItems joinTable: [name: 'order_invoice', key: 'order_item_id']
    }

    static transients = [
            "orderItemType",
            "quantityInStandardUom",
            "quantityRemaining",
            "quantityReceived",
            "quantityReceivedInStandardUom",
            "quantityCanceled",
            "quantityCanceledInStandardUom",
            "quantityShipped",
            "quantityShippedInStandardUom",
            "quantityInShipments",
            "quantityInShipmentsInStandardUom",
            "quantityAvailableToInvoice",
            "invoiceableShipmentItems",
            "quantityInvoicedInStandardUom",
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
            "disableRefresh",
            // Statuses
            "partiallyFulfilled",
            "completelyFulfilled",
            "completelyReceived",
            "partiallyReceived",
            "pending",
            "quantityRemainingToShip",
            "allInvoiceItems",
            "postedQuantityInvoiced",
            "postedQuantityInvoicedInStandardUom",
            "orderItemStatus",
            "canceled"
    ]

    static belongsTo = [order: Order, parentOrderItem: OrderItem]

    static hasMany = [orderItems: OrderItem, shipmentItems: ShipmentItem, orderAdjustments: OrderAdjustment, picklistItems: PicklistItem, invoiceItems: InvoiceItem]

    static constraints = {
        description(nullable: true)
        category(nullable: true)
        product(nullable: false)
        inventoryItem(nullable: true)
        requestedBy(nullable: true)
        quantity(nullable: false, min: 1)
        quantityUom(nullable: true)
        quantityPerUom(nullable: false, validator: { value -> value > 0 } )
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
            ValidationTagLib g = Holders.grailsApplication.mainContext.getBean(ValidationTagLib.class)
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

    Integer getQuantityCanceledInStandardUom() {
        return shippedShipmentItems?.sum { ShipmentItem shipmentItem ->
            shipmentItem?.quantityCanceled
        }?:0
    }

    Integer getQuantityShipped() {
        return quantityShippedInStandardUom / (quantityPerUom?:1)
    }

    Integer getQuantityReceived() {
        return quantityReceivedInStandardUom / (quantityPerUom?:1)
    }

    Integer getQuantityCanceled() {
        return quantityCanceledInStandardUom / (quantityPerUom?:1)
    }

    Integer getQuantityInShipments() {
        return quantityInShipmentsInStandardUom / (quantityPerUom?:1)
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
        if (canceled) {
            return 0
        }

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
        return (quantityReceived + quantityCanceled) >= quantity
    }

    Boolean isPartiallyReceived() {
        return quantityReceived > 0 && !isCompletelyReceived()
    }

    /**
     * Checks if everything is in posted regular invoices
     * */
    Boolean isCompletelyInvoiced() {
        // TODO: if this is still used, ensure that we want to compare standard uom below to non standard
        return postedQuantityInvoicedInStandardUom >= quantity
    }

    /**
     * Used in final invoices for prepaid invoices.
     * Checks if everything is in regular invoices (including non posted invoices)
     * */
    Boolean isFullyInvoiced() {
        if (canceled) {
            return hasRegularInvoice
        }

        return quantityInvoiced >= quantity
    }

    // Including not yet posted invoices
    Integer getQuantityInvoiced() {
        return allInvoiceItems
                ?.findAll { !it?.invoice?.isPrepaymentInvoice && !it.inverse }
                ?.sum { it.quantity } ?: 0
    }

    // Including not yet posted invoices
    Integer getQuantityInvoicedInStandardUom() {
        return quantityInvoiced * (quantityPerUom ?: 1)
    }

    Integer getQuantityAvailableToInvoice() {
        return canceled
                ? null
                : quantityShippedInStandardUom - quantityInvoicedInStandardUom
    }

    /**
     * Item can be on a regular invoice if it is not canceled or if it is canceled, but has prepayment
     * */
    Boolean canBeOnRegularInvoice() {
        if (canceled) {
            return hasPrepaymentInvoice
        }

        return true
    }

    /**
     * Used in final invoices for prepaid invoices. Item is invoiceable if:
     *  - is canceled and this item has prepayment invoice item (and was not yet invoiced),
     *  - is not canceled and is not yet fully invoiced and has quantity available to invoice
     *      (even on not yet posted invoices, >> and does not have to have prepaymnent invoice item <<),
     * */
    Boolean isInvoiceable() {
        if (canceled) {
            return hasPrepaymentInvoice && !fullyInvoiced
        }

        return !fullyInvoiced && quantityAvailableToInvoice > 0
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

    /**
     * Returns all invoice items related to this order item.
     * Either directly related to that order item or from related shipment items.
     **/
    List<InvoiceItem> getAllInvoiceItems() {
        (shipmentItems*.invoiceItems + invoiceItems)?.flatten()?.unique() ?: []
    }

    // Check quantity (standard uom) in posted invoices
    Integer getPostedQuantityInvoicedInStandardUom() {
        return postedQuantityInvoiced * (quantityPerUom ?: 1)
    }

    def getInvoices() {
        return allInvoiceItems*.invoice.unique()
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

    Integer getPostedQuantityInvoiced() {
        return allInvoiceItems?.findAll {
            it?.invoice?.datePosted != null && !it?.invoice?.isPrepaymentInvoice && !it.inverse
        }?.sum { it.quantity } ?: 0
    }

    Set<ShipmentItem> getInvoiceableShipmentItems() {
        return shipmentItems.findAll { it.invoiceable }
    }

    def totalQuantityPicked() {
        return PicklistItem.findAllByOrderItem(this).sum { it.quantity }
    }

    def retrievePicklistItems() {
        def picklistItems = PicklistItem.findAllByOrderItem(this)
        return picklistItems
    }

    /**
     * Fetching OrderItemSummary by OrderItem.
     * NOTE: This should not be use if there is option to fetch OrderItemSummary by Order
     *       Use order.getOrderItemsDerivedStatus() instead
     * */
    String getOrderItemStatus() {
        return OrderItemSummary.get(id)?.derivedStatus
    }

    Boolean isCanceled() {
        return orderItemStatusCode == OrderItemStatusCode.CANCELED
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
