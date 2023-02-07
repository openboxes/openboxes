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

import org.pih.warehouse.invoice.Invoice
import org.pih.warehouse.receiving.Receipt
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem
import org.springframework.context.ApplicationEvent

class RefreshOrderSummaryEvent extends ApplicationEvent {

    List<String> orderIds
    Boolean isDelete = Boolean.FALSE
    Boolean disableRefresh = Boolean.FALSE

    RefreshOrderSummaryEvent(Order source) {
        super(source)
        this.orderIds = source?.id && source?.isPurchaseOrder ? [source?.id] : []
        this.isDelete = isDelete
        this.disableRefresh = disableRefresh
    }

    RefreshOrderSummaryEvent(Order source, Boolean isDelete) {
        super(source)
        this.orderIds = source?.id && source?.isPurchaseOrder ? [source?.id] : []
        this.isDelete = isDelete
        this.disableRefresh = disableRefresh
    }

    RefreshOrderSummaryEvent(List<Order> source) {
        super(source)
        this.orderIds = source?.findAll {it?.isPurchaseOrder }?.collect { it.id } ?: []
        this.isDelete = isDelete
        this.disableRefresh = disableRefresh
    }

    RefreshOrderSummaryEvent(OrderItem source) {
        super(source)
        this.orderIds = source?.order?.id ? [source?.order?.id] : []
        this.isDelete = isDelete
        this.disableRefresh = source.disableRefresh
    }

    RefreshOrderSummaryEvent(Shipment source) {
        super(source)
        this.orderIds = source?.purchaseOrders?.collect { it.id } ?: []
        this.isDelete = isDelete
        this.disableRefresh = source.disableRefresh
    }

    RefreshOrderSummaryEvent(Receipt source) {
        super(source)
        this.orderIds = source?.shipment?.purchaseOrders?.collect { it.id } ?: []
        this.isDelete = isDelete
        this.disableRefresh = source.disableRefresh
    }

    RefreshOrderSummaryEvent(Invoice source) {
        super(source)
        this.orderIds = !source?.isPrepaymentInvoice ? (source?.orders?.findAll {  it?.isPurchaseOrder }?.collect { it.id } ?: []) : []
        this.isDelete = isDelete
        this.disableRefresh = source.disableRefresh
    }
}
