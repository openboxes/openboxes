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

import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.inventory.Transaction
import org.pih.warehouse.invoice.InvoiceItem
import org.pih.warehouse.picklist.PicklistItem
import org.pih.warehouse.receiving.ReceiptItem
import org.pih.warehouse.shipping.ShipmentItem
import org.springframework.context.ApplicationEvent

class RefreshOrderSummaryEvent extends ApplicationEvent {

    String orderId
    Boolean isDelete

    RefreshOrderSummaryEvent(Order order) {
        super(order)
        this.orderId = order?.id
        this.isDelete = false
    }

    RefreshOrderSummaryEvent(Order order, Boolean isDelete) {
        super(order)
        this.orderId = order?.id
        this.isDelete = isDelete
    }

    RefreshOrderSummaryEvent(OrderItem orderItem) {
        super(orderItem)
        this.orderId = orderItem?.order?.id
        this.isDelete = false
    }

    RefreshOrderSummaryEvent(OrderAdjustment orderAdjustment) {
        super(orderAdjustment)
        this.orderId = orderAdjustment?.order?.id
        this.isDelete = false
    }

    RefreshOrderSummaryEvent(ShipmentItem shipmentItem) {
        super(shipmentItem)
        this.orderId = shipmentItem?.orderId
        this.isDelete = false
    }

    RefreshOrderSummaryEvent(ReceiptItem receiptItem) {
        super(receiptItem)
        this.orderId = receiptItem?.shipmentItem?.orderId
        this.isDelete = false
    }

    RefreshOrderSummaryEvent(InvoiceItem invoiceItem) {
        super(invoiceItem)
        this.orderId = invoiceItem?.order?.id
        this.isDelete = false
    }
}
