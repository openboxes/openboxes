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

        BigDecimal prepaymentPercent = (order.paymentTerm?.prepaymentPercent ?: Constants.DEFAULT_PAYMENT_PERCENT) / 100

        order.activeOrderItems.each { OrderItem orderItem ->
            InvoiceItem invoiceItem = createFromOrderItem(orderItem)
            // This is prepayment item, we need to adjust the amount according to the payment terms
            invoiceItem.amount = invoiceItem.amount * prepaymentPercent
            invoice.addToInvoiceItems(invoiceItem)
        }

        order.activeOrderAdjustments.each { OrderAdjustment orderAdjustment ->
            InvoiceItem invoiceItem = createFromOrderAdjustment(orderAdjustment)
            // This is prepayment item, we need to adjust the amount according to the payment terms
            invoiceItem.amount = invoiceItem.amount * prepaymentPercent
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
                InvoiceItem inverseItem = createInverseItemForCanceledOrderItem(orderItem)
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
            InvoiceItem inverseItem = createInverseItemForOrderAdjustment(orderAdjustment)
            invoice.addToInvoiceItems(invoiceItem)
            invoice.addToInvoiceItems(inverseItem)
        }

        return invoice.save()
    }

    private InvoiceItem createFromOrderItem(OrderItem orderItem) {
        if (orderItem.orderItemStatusCode == OrderItemStatusCode.CANCELED) {
            InvoiceItem invoiceItem = new InvoiceItem(
                    quantity: 0,
                    product: orderItem.product,
                    glAccount: orderItem.glAccount ?: orderItem.product.glAccount,
                    budgetCode: orderItem?.budgetCode,
                    quantityUom: orderItem?.quantityUom,
                    quantityPerUom: orderItem.quantityPerUom ?: 1,
                    unitPrice: orderItem.unitPrice,
                    // canceled invoice items amount is 0
                    amount: 0
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
                unitPrice: orderItem.unitPrice,
                // For non-canceled order items invoice item amount is equal to (order item quantity * unit price)
                amount: (orderItem.quantity ?: 0) * (orderItem.unitPrice ?: 0.0)
        )
        invoiceItem.addToOrderItems(orderItem)
        return invoiceItem
    }

    private InvoiceItem createFromShipmentItem(ShipmentItem shipmentItem) {
        OrderItem orderItem = shipmentItem.orderItems?.find { it }
        Integer quantity = shipmentItem.quantityToInvoiceInStandardUom ? (shipmentItem.quantityToInvoiceInStandardUom / orderItem?.quantityPerUom) : 0
        InvoiceItem invoiceItem = new InvoiceItem(
                // InvoiceItem quantity is in UoM not in standard UoM
                quantity: quantity,
                product: shipmentItem.product,
                glAccount: shipmentItem.product.glAccount,
                budgetCode: orderItem?.budgetCode,
                quantityUom: orderItem?.quantityUom,
                quantityPerUom: orderItem?.quantityPerUom ?: 1,
                unitPrice: orderItem?.unitPrice,
                // For shipment items invoice item amount is equal to (shipped item quantity * unit price) in UoM quantity
                // and unit price is take from order item (in case it was updated)
                amount: quantity * orderItem?.unitPrice
        )
        invoiceItem.addToShipmentItems(shipmentItem)
        return invoiceItem
    }

    private InvoiceItem createFromOrderAdjustment(OrderAdjustment orderAdjustment) {
        InvoiceItem invoiceItem = new InvoiceItem(
                budgetCode: orderAdjustment.budgetCode,
                product: orderAdjustment.orderItem?.product,
                glAccount: orderAdjustment.glAccount ?: orderAdjustment.orderItem?.glAccount ?: orderAdjustment.orderAdjustmentType?.glAccount,
                quantity: orderAdjustment?.canceled ? 0 : 1,
                quantityUom: orderAdjustment.orderItem?.quantityUom,
                quantityPerUom: orderAdjustment.orderItem?.quantityPerUom ?: 1,
                unitPrice: orderAdjustment.totalAdjustments,
                // For non-canceled order adjustments amount is equal to the total adjustments
                amount: orderAdjustment?.canceled ? 0 : orderAdjustment.totalAdjustments
        )
        invoiceItem.addToOrderAdjustments(orderAdjustment)
        return invoiceItem
    }

    private InvoiceItem createInverseItemForCanceledOrderItem(OrderItem orderItem) {
        InvoiceItem prepaymentItem = orderItem.invoiceItems.find { it.isPrepaymentInvoice }
        InvoiceItem inverseItem = createFromOrderItem(orderItem)
        // For canceled order item take quantity from prepayment item
        inverseItem.quantity = prepaymentItem.quantity
        inverseItem.unitPrice = prepaymentItem.unitPrice
        // Multiplied by (-1) to keep inverse items as negative amount
        inverseItem.amount = prepaymentItem.amount * (-1)
        inverseItem.inverse = true
        return inverseItem
    }

    private InvoiceItem createInverseItemForShipmentItem(ShipmentItem shipmentItem, InvoiceItem invoiceItem) {
        OrderItem orderItem = shipmentItem.orderItems?.find { it }
        BigDecimal prepaymentPercent = (orderItem.order.paymentTerm?.prepaymentPercent ?: Constants.DEFAULT_PAYMENT_PERCENT) / 100
        InvoiceItem prepaymentItem = orderItem.invoiceItems.find { it.isPrepaymentInvoice }
        InvoiceItem inverseItem = createFromShipmentItem(shipmentItem)
        // For shipment items we have to check if the ordered quantity was edited after prepayment was generated.
        // If that was the case we have to check maximum quantity available to inverse
        Integer quantityInverseable = getQuantityAvailableToInverse(orderItem, prepaymentItem)
        if (!quantityInverseable || quantityInverseable < 0) {
            return null
        }
        Integer quantity = invoiceItem.quantity >= quantityInverseable ? quantityInverseable : invoiceItem.quantity
        inverseItem.quantity = quantity
        inverseItem.inverse = true
        inverseItem.unitPrice = prepaymentItem.unitPrice
        // Multiplied by (-1) to keep inverse items as negative amount
        // unit price is taken from prepayment item (to not accidentally overwrite inverse with changed unit price)
        inverseItem.amount = quantity * prepaymentItem.unitPrice * prepaymentPercent * (-1)
        return inverseItem
    }

    private InvoiceItem createInverseItemForOrderAdjustment(OrderAdjustment orderAdjustment) {
        InvoiceItem prepaymentItem = orderAdjustment.invoiceItems.find { it.isPrepaymentInvoice }
        InvoiceItem inverseItem = createFromOrderAdjustment(orderAdjustment)
        // For order adjustment invoiceItem.quantity is 1 or 0, but for inverse should be for now 1
        inverseItem.quantity = 1
        inverseItem.inverse = true
        inverseItem.unitPrice = prepaymentItem.unitPrice
        // Multiplied by (-1) to keep inverse items as negative amount
        // unit price is taken from prepayment item (to not accidentally overwrite inverse with changed unit price)
        inverseItem.amount = prepaymentItem.amount * (-1)
        return inverseItem
    }

    private Integer getQuantityAvailableToInverse(OrderItem orderItem, InvoiceItem prepaymentItem) {
        Integer quantityInversed = orderItem.allInvoiceItems.findAll { it.inverse }.sum { it.quantity } ?: 0
        return prepaymentItem.quantity > quantityInversed ? prepaymentItem.quantity - quantityInversed : 0
    }

    /**
     * Removes invoice item for the itemId and finds related inverse item and removes it too
     * */
    void removeInvoiceItem(String itemId) {
        InvoiceItem invoiceItem = InvoiceItem.get(itemId)
        if (!invoiceItem) {
            throw new IllegalArgumentException("Missing invoice item to delete")
        }
        if (invoiceItem.isPrepaymentInvoice || invoiceItem.inverse) {
            throw new IllegalArgumentException("Cannot delete prepayment or inverse items")
        }

        InvoiceItem inverseItem = findInverseItem(invoiceItem)
        if (inverseItem) {
            deleteInvoiceItem(inverseItem)
        }
        deleteInvoiceItem(invoiceItem)
    }

    /**
     * Clean up related objects and delete item (due to many-to-many relation, we have to clean this to not get it
     * overwritten by the "InvalidDataAccessApiUsageException: deleted object would be re-saved by cascade")
     * */
    private void deleteInvoiceItem(InvoiceItem invoiceItem) {
        invoiceItem.orderAdjustments?.each { OrderAdjustment oa -> oa.removeFromInvoiceItems(invoiceItem) }
        invoiceItem.orderItems?.each { OrderItem oi -> oi.removeFromInvoiceItems(invoiceItem) }
        invoiceItem.shipmentItems?.each { ShipmentItem si -> si.removeFromInvoiceItems(invoiceItem) }

        Invoice invoice = invoiceItem.invoice
        invoice.removeFromInvoiceItems(invoiceItem)

        invoiceItem.delete()
    }

    private InvoiceItem findInverseItem(InvoiceItem invoiceItem) {
        Invoice invoice = invoiceItem.invoice

        // Since we can have only one of the three options I am doing this in this way (at least for now)
        def relatedObject = invoiceItem.orderAdjustment ?: invoiceItem.orderItem ?: invoiceItem.shipmentItem ?: null

        // To be checked - If one invoice can have more inverse items for the same shipment item - IMHO it should not
        // be possible, because there is only one invoice item per shipment item in that case and we don't have an option
        // to generate invoice item partially for the same invoice
        return relatedObject?.invoiceItems?.find { InvoiceItem it -> it.inverse && it.invoice == invoice }
    }
}
