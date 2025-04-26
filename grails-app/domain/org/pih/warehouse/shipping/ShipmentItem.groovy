/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 **/
package org.pih.warehouse.shipping

import org.pih.warehouse.core.ActivityCode
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.donation.Donor
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.LotStatusCode
import org.pih.warehouse.invoice.InvoiceItem
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.product.Product
import org.pih.warehouse.receiving.Receipt
import org.pih.warehouse.receiving.ReceiptItem
import org.pih.warehouse.receiving.ReceiptStatusCode
import org.pih.warehouse.requisition.RequisitionItem


class ShipmentItem implements Comparable, Serializable {

    String id
    String lotNumber            // Loose coupling to the inventory lot
    Date expirationDate
    Product product                // Specific product that we're tracking
    Integer quantity            // Quantity could be a class on its own
    Person recipient            // Recipient of an item
    Donor donor                    // Organization that donated the goods
    Date dateCreated
    Date lastUpdated
    InventoryItem inventoryItem
    Container container
    Location binLocation
    // within.  This is different from the container type
    // (which might be a pallet or shipping container), in
    // that this will likely be a box that the item is
    // actually contained within.

    Shipment shipment
    RequisitionItem requisitionItem

    Integer sortOrder

    static belongsTo = [Shipment, OrderItem]

    static hasMany = [orderItems: OrderItem, receiptItems: ReceiptItem, invoiceItems: InvoiceItem]

    static transients = [
            "comments",
            "orderItemId",
            "quantityReceivedAndCanceled",
            "quantityCanceled",
            "quantityReceived",
            "quantityRemaining",
            "orderNumber",
            "orderId",
            "purchaseOrders",
            "orderName",
            "quantityRemainingToShip",
            "quantityToInvoiceInStandardUom",
            "quantityPerUom",
            "hasRecalledLot",
            "quantityPicked",
            "quantityPickedFromOrders",
            "unavailableQuantityPicked",
            "paymentTerm",
            "quantityInvoiced",
            "orderItem",
            "unitOfMeasure",
    ]

    static mapping = {
        id generator: 'uuid'
        cache true
        orderItems joinTable: [name: 'order_shipment', key: 'shipment_item_id']
        invoiceItems joinTable: [name: 'shipment_invoice', key: 'shipment_item_id']
    }

    static constraints = {
        binLocation(nullable: true)
        container(nullable: true)
        product(nullable: false)
        // TODO: this doesn't seem to prevent the product field from being empty
        lotNumber(nullable: true, maxSize: 255)
        expirationDate(nullable: true)
        quantity(min: 0, range: 0..2147483646)
        recipient(nullable: true)
        inventoryItem(nullable: true)
        donor(nullable: true)
        requisitionItem(nullable: true)
        shipment(nullable: true)
        sortOrder(nullable: true)
    }

    Boolean isFullyReceived() {
        return quantityReceivedAndCanceled >= quantity
    }

    /**
     * @return the lot number of the inventory item (or the lot number of the shipment item for backwards compatibility)
     */
    def getLotNumber() {
        return inventoryItem?.lotNumber ?: lotNumber
    }

    /**
     * @return the expiration date of the inventory item (or the expiration date of the shipment item for backwards compatibility)
     */
    def getExpirationDate() {
        return inventoryItem?.expirationDate ?: expirationDate
    }

    String getOrderItemId() {
        def orderItemIds = orderItems?.collect { OrderItem orderItem -> orderItem.id }?.unique()
        return orderItemIds ? orderItemIds.first() : null
    }

    String getOrderId() {
        def orderNumbers = orderItems?.collect { OrderItem orderItem -> orderItem.order.id }?.unique()
        return orderNumbers ? orderNumbers.first() : ''
    }

    List<Order> getPurchaseOrders() {
        def orders = orderItems?.order?.unique()
        return orders?.findAll { it.isPurchaseOrder}
    }

    String getOrderNumber() {
        def orderNumbers = orderItems?.collect { OrderItem orderItem -> orderItem.order.orderNumber }?.unique()
        return orderNumbers ? orderNumbers.first() : ''
    }

    String getOrderName() {
        def orderNames = orderItems?.collect { OrderItem orderItem -> orderItem.order.name }?.unique()
        return orderNames ? orderNames.first() : ''
    }

    String getPaymentTerm() {
        List<String> orderPaymentTerms = orderItems?.collect { OrderItem orderItem -> orderItem.order.paymentTerm?.name }?.unique()
        return orderPaymentTerms ? orderPaymentTerms.first() : ''
    }

    def totalQuantityShipped() {
        int totalQuantityShipped = 0
        // Should use inventory item instead of comparing product & lot number
        if (shipment.shipmentItems) {
            shipment.shipmentItems.each {
                if (it.product == this.product && it.lotNumber == this.lotNumber) {
                    totalQuantityShipped += it.quantity
                }
            }
        }
        return totalQuantityShipped
    }

    def totalQuantityReceived() {
        int totalQuantityReceived = 0
        // Should use inventory item instead of comparing product & lot number
        if (shipment.receipts) {
            shipment.receipts.each { Receipt receipt ->
                if (receipt) {
                    receipt.receiptItems.each {
                        if (it.product == this.product && it.lotNumber == this.lotNumber) {
                            totalQuantityReceived += it.quantityReceived
                        }
                    }
                }
            }
        }
        return totalQuantityReceived
    }

    /**
     * @deprecated
     * @return
     */
    Integer quantityReceived() {
        return quantityReceived
    }

    Integer getQuantityReceived() {
        return (receiptItems) ? receiptItems.sum { ReceiptItem receiptItem ->
            ReceiptStatusCode.RECEIVED == receiptItem?.receipt?.receiptStatusCode && receiptItem?.product == product &&
                    receiptItem?.quantityReceived ? receiptItem.quantityReceived : 0
        } : 0
    }

    /**
     * @deprecated
     * @return
     */
    Integer quantityCanceled() {
        return quantityCanceled
    }

    Integer getQuantityCanceled() {
        return (receiptItems) ? receiptItems.sum { ReceiptItem receiptItem ->
            ReceiptStatusCode.RECEIVED == receiptItem?.receipt?.receiptStatusCode && receiptItem?.product == product &&
                    receiptItem?.quantityCanceled ? receiptItem.quantityCanceled : 0
        } : 0
    }

    Integer getQuantityRemaining() {
        return quantity - quantityReceivedAndCanceled
    }

    Integer getQuantityReceivedAndCanceled() {
        return quantityReceived + quantityCanceled
    }

    Integer getQuantityRemainingToShip() {
        return orderItem ? orderItem.getQuantityRemainingToShip(shipment) : 0
    }

    BigDecimal getQuantityPerUom() {
        return orderItem ? orderItem.quantityPerUom : 1
    }

    Integer getQuantityPicked() {
        Integer quantityPicked
        if (binLocation) {
            quantityPicked = requisitionItem?.picklistItems?.findAll { it.inventoryItem == inventoryItem && it.binLocation == binLocation }?.sum { it.quantityPicked }
        } else {
            quantityPicked = requisitionItem?.picklistItems?.findAll { it.inventoryItem == inventoryItem }?.sum { it.quantityPicked }
        }
        return quantityPicked?:quantity
    }

    // For requisition based shipments, to avoid qualifying recalled or on hold items into quantity picked validation
    Integer getUnavailableQuantityPicked() {
        Integer unavailableQuantityPicked
        if (binLocation) {
            unavailableQuantityPicked = requisitionItem?.picklistItems?.findAll {
                it.inventoryItem == inventoryItem && it.binLocation == binLocation && (
                    it.inventoryItem?.recalled || it.binLocation?.supports(ActivityCode.HOLD_STOCK)
                )
            }?.sum { it.quantity }
        } else {
            unavailableQuantityPicked = requisitionItem?.picklistItems?.findAll {
                it.inventoryItem == inventoryItem && it.inventoryItem?.recalled
            }?.sum { it.quantity }
        }
        return unavailableQuantityPicked?:0
    }

    Integer getQuantityPickedFromOrders() {
        Integer quantityPicked
        if (binLocation) {
            quantityPicked = orderItems.collect { it.picklistItems?.findAll { it.inventoryItem == inventoryItem && it.binLocation == binLocation }?.sum { it.quantityPicked } }.sum()
        } else {
            quantityPicked = orderItems.collect { it.picklistItems?.findAll { it.inventoryItem == inventoryItem }?.sum { it.quantityPicked } }.sum()
        }
        return quantityPicked?:quantity
    }


    String[] getComments() {
        def comments = []
        if (receiptItems) {
            comments = receiptItems?.comment?.findAll { it }
        }
        return comments
    }

    Boolean getHasRecalledLot() {
        return inventoryItem?.lotStatus == LotStatusCode.RECALLED
    }

    // quantity invoiced in uom
    Integer getQuantityInvoiced() {
        return invoiceItems?.findAll { !it.inverse } ?.sum { it.quantity ?: 0 } ?: 0
    }

    Integer getQuantityInvoicedInStandardUom() {
        return quantityInvoiced * quantityPerUom
    }

    Integer getQuantityToInvoiceInStandardUom() {
        // ShipmentItem.quantity is in standard uom
        return quantity - quantityInvoicedInStandardUom
    }

    Integer getQuantityToInvoice() {
        return quantityToInvoiceInStandardUom / quantityPerUom
    }

    Boolean isInvoiceable() {
        return quantityToInvoiceInStandardUom > 0
    }

    OrderItem getOrderItem() {
        return orderItems[0]
    }

    String getUnitOfMeasure() {
        return orderItem?.unitOfMeasure
    }

    BigDecimal getPackSize() {
        return orderItem?.quantityPerUom
    }

    /**
     * Sorts shipping items by associated product name, then lot number, then quantity,
     * and finally by id.
     *
     * FIXME Need to get rid of the product and lot number comparison
     */
    int compareTo(obj) {
        def sortOrder =
                container?.parentContainer?.sortOrder <=> obj?.container?.parentContainer?.sortOrder ?:
                        container?.sortOrder <=> obj?.container?.sortOrder ?:
                                inventoryItem?.product?.name <=> obj?.inventoryItem?.product?.name ?:
                                        product?.name <=> obj?.product?.name ?:
                                                inventoryItem?.lotNumber <=> obj?.inventoryItem?.lotNumber ?:
                                                        lotNumber <=> obj?.lotNumber ?:
                                                                binLocation?.name <=> obj?.binLocation?.name ?:
                                                                        quantity <=> obj?.quantity ?:
                                                                                id <=> obj?.id
        return sortOrder
    }

    ShipmentItem cloneShipmentItem() {
        return new ShipmentItem(
                lotNumber: this.lotNumber,
                expirationDate: this.expirationDate,
                product: this.product,
                inventoryItem: this.inventoryItem,
                binLocation: this.binLocation,
                quantity: this.quantity,
                recipient: this.recipient,
                donor: this.donor,
                container: this.container
        )
    }
}
