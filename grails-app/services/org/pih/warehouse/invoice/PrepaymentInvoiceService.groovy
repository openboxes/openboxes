package org.pih.warehouse.invoice

import org.pih.warehouse.core.Constants
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderAdjustment
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.order.OrderItemStatusCode
import org.pih.warehouse.shipping.ShipmentItem
import grails.gorm.transactions.Transactional

@Transactional
class PrepaymentInvoiceService {

    InvoiceService invoiceService

    Invoice generateInvoice(Order order) {
        if (!order.hasPrepaymentInvoice) {
            throw new Exception("This order has no prepayment invoice")
        }

        Invoice invoice = invoiceService.createFromOrder(order)
        invoiceService.createOrUpdateVendorInvoiceNumber(invoice, order.orderNumber)

        order.invoiceableOrderItems.each { OrderItem orderItem ->
            if (orderItem.orderItemStatusCode == OrderItemStatusCode.CANCELED) {
                InvoiceItem invoiceItem = invoiceService.createFromOrderItem(orderItem)
                invoice.addToInvoiceItems(invoiceItem)
            } else {
                orderItem?.invoiceableShipmentItems?.each { ShipmentItem shipmentItem ->
                    InvoiceItem invoiceItem = invoiceService.createFromShipmentItem(shipmentItem)
                    invoice.addToInvoiceItems(invoiceItem)
                }
            }
        }

        order.invoiceableAdjustment.each { OrderAdjustment orderAdjustment ->
            InvoiceItem invoiceItem = invoiceService.createFromOrderAdjustment(orderAdjustment)
            invoice.addToInvoiceItems(invoiceItem)
        }

        return invoice.save()
    }

    Invoice generatePrepaymentInvoice(Order order) {
        if (order.orderItems.any { it.hasInvoices } || order.orderAdjustments.any { it.hasInvoices }) {
            throw new Exception("Some order items or order adjustments for this order already have been invoiced")
        }

        Invoice invoice = invoiceService.createFromOrder(order)
        invoice.invoiceType = InvoiceType.findByCode(InvoiceTypeCode.PREPAYMENT_INVOICE)
        invoiceService.createOrUpdateVendorInvoiceNumber(invoice, order.orderNumber + Constants.PREPAYMENT_INVOICE_SUFFIX)

        if (order?.orderItems?.empty && order?.orderAdjustments?.empty) {
            throw new Exception("No order items or order adjustments found for given order")
        }

        order.activeOrderItems.each { OrderItem orderItem ->
            InvoiceItem invoiceItem = invoiceService.createFromOrderItem(orderItem)
            invoice.addToInvoiceItems(invoiceItem)
        }

        order.activeOrderAdjustments.each { OrderAdjustment orderAdjustment ->
            InvoiceItem invoiceItem = invoiceService.createFromOrderAdjustment(orderAdjustment)
            invoice.addToInvoiceItems(invoiceItem)
        }

        return invoice.save()
    }
}
