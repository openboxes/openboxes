package org.pih.warehouse.invoice

import grails.core.GrailsApplication
import org.pih.warehouse.core.Constants
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderAdjustment
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.order.OrderItemStatusCode
import org.pih.warehouse.shipping.ShipmentItem
import grails.gorm.transactions.Transactional

@Transactional
class PrepaymentInvoiceService {

    GrailsApplication grailsApplication
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
            // This is prepayment item, we need to adjust the amount according to the payment terms
            invoiceItem.amount = invoiceItem.amount * order.prepaymentPercent
            invoice.addToInvoiceItems(invoiceItem)
        }

        order.activeOrderAdjustments.each { OrderAdjustment orderAdjustment ->
            InvoiceItem invoiceItem = createFromOrderAdjustment(orderAdjustment)
            // This is prepayment item, we need to adjust the amount according to the payment terms
            invoiceItem.amount = invoiceItem.amount * order.prepaymentPercent
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
                if (inverseItem) {
                    invoice.addToInvoiceItems(inverseItem)
                }
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
            if (inverseItem) {
                invoice.addToInvoiceItems(inverseItem)
            }
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
        if (!prepaymentItem) {
            return null
        }
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
        InvoiceItem prepaymentItem = orderItem.invoiceItems.find { it.isPrepaymentInvoice }
        if (!prepaymentItem) {
            return null
        }
        // For shipment items we have to check if the ordered quantity was edited after prepayment was generated.
        // If that was the case we have to check maximum quantity available to inverse
        Integer quantityInverseable = getQuantityAvailableToInverse(orderItem, prepaymentItem)
        if (!quantityInverseable || quantityInverseable < 0) {
            return null
        }
        InvoiceItem inverseItem = createFromShipmentItem(shipmentItem)
        Integer quantity = getQuantityToInverse(orderItem, invoiceItem.quantity, quantityInverseable)
        inverseItem.quantity = quantity
        inverseItem.inverse = true
        inverseItem.unitPrice = prepaymentItem.unitPrice
        // Multiplied by (-1) to keep inverse items as negative amount
        // unit price is taken from prepayment item (to not accidentally overwrite inverse with changed unit price)
        inverseItem.amount = quantity * prepaymentItem.unitPrice * orderItem.order.prepaymentPercent * (-1)
        return inverseItem
    }

    private InvoiceItem createInverseItemForOrderAdjustment(OrderAdjustment orderAdjustment) {
        InvoiceItem prepaymentItem = orderAdjustment.invoiceItems.find { it.isPrepaymentInvoice }
        if (!prepaymentItem) {
            return null
        }
        if (orderAdjustment.inversedQuantity > 0) {
            return null
        }

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
        def g = grailsApplication.mainContext.getBean('org.grails.plugins.web.taglib.ApplicationTagLib')
        InvoiceItem invoiceItem = InvoiceItem.get(itemId)
        if (!invoiceItem) {
            throw new IllegalArgumentException("Missing invoice item to delete")
        }
        if (invoiceItem.isPrepaymentInvoice || invoiceItem.inverse) {
            throw new IllegalArgumentException("Cannot delete prepayment or inverse items")
        }
        if (invoiceItem.invoice.datePosted) {
            String defaultMessage = "Cannot update posted invoices"
            throw new IllegalArgumentException(g.message(code: "invoice.cannotUpdatePosted.error", default: defaultMessage))
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
        invoiceItem.invoice.removeFromInvoiceItems(invoiceItem)
        invoiceItem.delete()
    }

    private InvoiceItem findInverseItem(InvoiceItem invoiceItem) {
        Invoice invoice = invoiceItem.invoice

        // Since we can have only one of the three options I am doing this in this way (at least for now)
        def relatedObject = invoiceItem.orderAdjustment ?: invoiceItem.orderItem ?: invoiceItem.shipmentItem

        // To be checked - If one invoice can have more inverse items for the same shipment item - IMHO it should not
        // be possible, because there is only one invoice item per shipment item in that case and we don't have an option
        // to generate invoice item partially for the same invoice
        return relatedObject?.invoiceItems?.find { InvoiceItem it -> it.inverse && it.invoice == invoice }
    }

    /**
     * Bulk editing invoice items quantity on final invoice for prepaid POs.
     * We are allowing to edit only the quantity here. After editing invoice items quantity we have to
     * change quantity on the corresponding inverse item.
     *
     * */
    void updateItems(String invoiceId, List items) {
        def g = grailsApplication.mainContext.getBean('org.grails.plugins.web.taglib.ApplicationTagLib')
        Invoice invoice = Invoice.get(invoiceId)
        if (!invoice) {
            String defaultMessage = "Cannot find invoice with id: ${invoiceId}"
            throw new IllegalArgumentException(g.message(code: "invoice.cannotFind.label", args: [invoiceId], default: defaultMessage))
        }
        if (invoice.isPrepaymentInvoice || !invoice.hasPrepaymentInvoice) {
            String defaultMessage = "Cannot update quantities on prepayment or non prepaid invoices"
            throw new IllegalArgumentException(g.message(code: "invoice.cannotUpdate.label", default: defaultMessage))
        }

        items.each { Map item ->
            updateInvoiceItemQuantity(item.id, item.quantity)
        }
    }

    /**
     * Updates invoice item's quantity and amount and finds related inverse item and updates quantity and amount
     * */
    void updateInvoiceItemQuantity(String itemId, Integer quantity) {
        def g = grailsApplication.mainContext.getBean('org.grails.plugins.web.taglib.ApplicationTagLib')
        InvoiceItem invoiceItem = InvoiceItem.get(itemId)
        if (!invoiceItem) {
            String defaultMessage = "Cannot find invoice item with id: ${itemId}"
            throw new IllegalArgumentException(g.message(code: "invoiceItem.cannotFind.error", args: [itemId], default: defaultMessage))
        }
        if (invoiceItem.invoice.datePosted) {
            String defaultMessage = "Cannot update posted invoices"
            throw new IllegalArgumentException(g.message(code: "invoice.cannotUpdatePosted.error", default: defaultMessage))
        }
        if (quantity <= 0) {
            String defaultMessage = "Quantity to change needs to be higher than 0"
            throw new IllegalArgumentException(g.message(code: "invoiceItem.quantityTooLow.error", default: defaultMessage))
        }
        if (invoiceItem?.inverse) {
            String defaultMessage = "Cannot edit inverse invoice items directly"
            throw new IllegalArgumentException(g.message(code: "invoiceItem.cannotEditInverse.error", default: defaultMessage))
        }
        if (!invoiceItem?.shipmentItem) {
            String defaultMessage = "Invoice item is missing shipment item. Cannot edit quantity."
            throw new IllegalArgumentException(g.message(code: "invoiceItem.missingShipment.error", default: defaultMessage))
        }
        Integer quantityAvailableToInvoice = invoiceItem.shipmentItem.quantityToInvoice
        if (quantity > quantityAvailableToInvoice + invoiceItem.quantity) {
            String defaultMessage = "Cannot update quantity to higher value than available to invoice"
            throw new IllegalArgumentException(g.message(code: "invoiceItem.quantityTooHigh.error", default: defaultMessage))
        }
        // update invoice item's quantity (and adjust amount)
        invoiceItem.quantity = quantity
        invoiceItem.amount = quantity * invoiceItem.unitPrice
        // update inverse item's quantity if there is inverse item (and adjust amount)
        InvoiceItem inverseItem = findInverseItem(invoiceItem)
        if (inverseItem) {
            OrderItem orderItem = invoiceItem?.shipmentItem?.orderItems?.find { it }
            InvoiceItem prepaymentItem = orderItem.invoiceItems.find { it.isPrepaymentInvoice }
            // find quantity that is still available to inverse and add to the current quantity from this inverse item
            // this needs to be checked because we can also increase invoiced quantity here
            Integer quantityAvailableToInverse = inverseItem.quantity + getQuantityAvailableToInverse(orderItem, prepaymentItem)
            Integer quantityToInverse = getQuantityToInverse(orderItem, quantity, quantityAvailableToInverse)
            inverseItem.quantity = quantityToInverse
            inverseItem.amount = quantityToInverse * prepaymentItem.unitPrice * orderItem.order.prepaymentPercent * (-1)
            return
        }

        // If there is a case that we did not found inverse item, but it was made available to inverse now after
        // editing or removing invoice items on invoices, lets try to create it here
        inverseItem = createInverseItemForShipmentItem(invoiceItem?.shipmentItem, invoiceItem)
        if (inverseItem) {
            invoiceItem.invoice.addToInvoiceItems(inverseItem)
        }
    }

    Integer getQuantityToInverse(OrderItem orderItem, Integer quantityInvoiced, Integer quantityInverseable) {
        if (orderItem.isCompletelyFulfilled() && orderItem.isFullyInvoiced()) {
            // If quantity if fully shipped and fully invoiced set full inverse quantity
            return quantityInverseable
        }

        return quantityInvoiced >= quantityInverseable ? quantityInverseable : quantityInvoiced
    }
}
