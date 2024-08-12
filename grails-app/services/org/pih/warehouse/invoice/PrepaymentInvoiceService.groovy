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
            InvoiceItem invoiceItem = createFromOrderItem(orderItem)
            invoiceItem.invoiceItemType = InvoiceItemType.PREPAYMENT
            invoice.addToInvoiceItems(invoiceItem)
        }

        order.activeOrderAdjustments.each { OrderAdjustment orderAdjustment ->
            InvoiceItem invoiceItem = createFromOrderAdjustment(orderAdjustment)
            invoiceItem.invoiceItemType = InvoiceItemType.PREPAYMENT
            invoice.addToInvoiceItems(invoiceItem)
        }

        return invoice.save()
    }

    Invoice generateInvoice(Order order) {
        if (!order.hasPrepaymentInvoice) {
            throw new Exception("This order has no prepayment invoice")
        }

        Invoice invoice = invoiceService.createFromOrder(order)
        invoiceService.createOrUpdateVendorInvoiceNumber(invoice, order.orderNumber)

        order.invoiceableOrderItems.each { OrderItem orderItem ->
            if (orderItem.orderItemStatusCode == OrderItemStatusCode.CANCELED) {
                InvoiceItem invoiceItem = createFromOrderItem(orderItem)
                InvoiceItem inverseItem = createInverseItemForOrderItem(orderItem)
                invoice.addToInvoiceItems(invoiceItem)
                invoice.addToInvoiceItems(inverseItem)
                return
            }

            orderItem?.invoiceableShipmentItems?.each { ShipmentItem shipmentItem ->
                InvoiceItem invoiceItem = createFromShipmentItem(shipmentItem)
                InvoiceItem inverseItem = createInverseItemForShipmentItem(shipmentItem, invoiceItem)
                invoice.addToInvoiceItems(invoiceItem)
                if (inverseItem) {
                    invoice.addToInvoiceItems(inverseItem)
                }
            }
        }

        order.invoiceableAdjustments.each { OrderAdjustment orderAdjustment ->
            InvoiceItem invoiceItem = createFromOrderAdjustment(orderAdjustment)
            InvoiceItem inverseItem = createInverseItemForOrderAdjustment(orderAdjustment, invoiceItem)
            invoice.addToInvoiceItems(invoiceItem)
            invoice.addToInvoiceItems(inverseItem)
        }

        return invoice.save()
    }

    InvoiceItem createFromOrderItem(OrderItem orderItem) {
        if (orderItem.orderItemStatusCode == OrderItemStatusCode.CANCELED) {
            InvoiceItem invoiceItem = new InvoiceItem(
                    quantity: 0,
                    product: orderItem.product,
                    glAccount: orderItem.glAccount ?: orderItem.product.glAccount,
                    budgetCode: orderItem?.budgetCode,
                    quantityUom: orderItem?.quantityUom,
                    quantityPerUom: orderItem.quantityPerUom ?: 1,
                    unitPrice: orderItem.unitPrice
            )
            invoiceItem.addToOrderItems(orderItem)
            return invoiceItem
        }

        InvoiceItem invoiceItem = new InvoiceItem(
                quantity: orderItem.quantity,
                product: orderItem.product,
                glAccount: orderItem.glAccount ?: orderItem.product.glAccount,
                budgetCode: orderItem?.budgetCode,
                quantityUom: orderItem?.quantityUom,
                quantityPerUom: orderItem.quantityPerUom ?: 1,
                unitPrice: orderItem.unitPrice
        )
        invoiceItem.addToOrderItems(orderItem)
        return invoiceItem
    }

    InvoiceItem createFromShipmentItem(ShipmentItem shipmentItem) {
        OrderItem orderItem = shipmentItem.orderItems?.find { it }
        InvoiceItem invoiceItem = new InvoiceItem(
                // InvoiceItem quantity is in UoM not in standard UoM
                quantity: shipmentItem.quantityToInvoiceInStandardUom ? (shipmentItem.quantityToInvoiceInStandardUom / orderItem?.quantityPerUom) : 0,
                product: shipmentItem.product,
                glAccount: shipmentItem.product.glAccount,
                budgetCode: orderItem?.budgetCode,
                quantityUom: orderItem?.quantityUom,
                quantityPerUom: orderItem?.quantityPerUom ?: 1,
                unitPrice: orderItem?.unitPrice
        )
        invoiceItem.addToShipmentItems(shipmentItem)
        return invoiceItem
    }

    InvoiceItem createFromOrderAdjustment(OrderAdjustment orderAdjustment) {
        InvoiceItem invoiceItem = new InvoiceItem(
                budgetCode: orderAdjustment.budgetCode,
                product: orderAdjustment.orderItem?.product,
                glAccount: orderAdjustment.glAccount ?: orderAdjustment.orderItem?.glAccount ?: orderAdjustment.orderAdjustmentType?.glAccount,
                quantity: orderAdjustment?.canceled ? 0 : 1,
                quantityUom: orderAdjustment.orderItem?.quantityUom,
                quantityPerUom: orderAdjustment.orderItem?.quantityPerUom ?: 1,
                unitPrice: orderAdjustment.totalAdjustments
        )
        invoiceItem.addToOrderAdjustments(orderAdjustment)
        return invoiceItem
    }

    InvoiceItem createInverseItemForOrderItem(OrderItem orderItem) {
        InvoiceItem prepaymentItem = orderItem.invoiceItems.find { it.isPrepaymentInvoice }
        InvoiceItem inverseItem = createFromOrderItem(orderItem)
        // For canceled order item take quantity from prepayment item
        // For now temporarily storing this as negative quantity
        inverseItem.quantity = prepaymentItem.quantity * (-1)
        inverseItem.unitPrice = prepaymentItem.unitPrice
        inverseItem.invoiceItemType = InvoiceItemType.INVERSE
        return inverseItem
    }

    InvoiceItem createInverseItemForShipmentItem(ShipmentItem shipmentItem, InvoiceItem invoiceItem) {
        OrderItem orderItem = shipmentItem.orderItems?.find { it }
        InvoiceItem prepaymentItem = orderItem.invoiceItems.find { it.isPrepaymentInvoice }
        InvoiceItem inverseItem = createFromShipmentItem(shipmentItem)
        // For shipment items we have to check if the ordered quantity was edited after prepayment was generated.
        // If that was the case we have to check maximum quantity available to inverse
        Integer quantityInverseable = getQuantityAvailableToInverse(orderItem, prepaymentItem)
        if (!quantityInverseable || quantityInverseable < 0) {
            return null
        }
        Integer quantity = invoiceItem.quantity >= quantityInverseable ? quantityInverseable : invoiceItem.quantity
        // For now temporarily storing this as negative quantity
        inverseItem.quantity = quantity * (-1)
        inverseItem.invoiceItemType = InvoiceItemType.INVERSE
        inverseItem.unitPrice = prepaymentItem.unitPrice
        return inverseItem
    }

    InvoiceItem createInverseItemForOrderAdjustment(OrderAdjustment orderAdjustment, InvoiceItem invoiceItem) {
        InvoiceItem prepaymentItem = orderAdjustment.invoiceItems.find { it.isPrepaymentInvoice }
        InvoiceItem inverseItem = createFromOrderAdjustment(orderAdjustment)
        // For order adjustment invoiceItem.quantity is 1 or 0, but for inverse should be for now -1
        // For now temporarily storing this as negative quantity
        inverseItem.quantity = -1
        inverseItem.invoiceItemType = InvoiceItemType.INVERSE
        inverseItem.unitPrice = prepaymentItem.unitPrice
        return inverseItem
    }

    Integer getQuantityAvailableToInverse(OrderItem orderItem, InvoiceItem prepaymentItem) {
        Integer quantityInversed = orderItem.allInvoiceItems.findAll { it.inverseItem }.sum { it.quantity } ?: 0
        // Because temporarily quantity inversed is negative we have to multiply it by -1 to get correct quantity to inverse
        return prepaymentItem.quantity > (quantityInversed * (-1)) ? prepaymentItem.quantity - (quantityInversed * (-1)) : 0
    }
}
