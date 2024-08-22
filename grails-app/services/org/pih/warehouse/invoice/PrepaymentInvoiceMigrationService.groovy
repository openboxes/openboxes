package org.pih.warehouse.invoice

import grails.gorm.transactions.Transactional

import org.pih.warehouse.core.Constants
import org.pih.warehouse.order.OrderAdjustment
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.order.OrderItemStatusCode
import org.pih.warehouse.shipping.ShipmentItem

/**
 * This service exists for the sole purpose of performing data migrations for changelog 0.9.x/2024-08-20-0000.
 * The logic was only put in a service class so that it could be unit tested. Do not use it in regular flows.
 */
@Transactional
class PrepaymentInvoiceMigrationService {

    PrepaymentInvoiceService prepaymentInvoiceService

    /**
     * For STEP 1 of the migration.
     *
     * Set the amount field for all prepayment invoice items that existed before the partial invoicing
     * feature was introduced.
     */
    void updateAmountFieldForPrepaymentInvoiceItems() {
        InvoiceType prepaymentInvoiceType = InvoiceType.findByCode(InvoiceTypeCode.PREPAYMENT_INVOICE)

        Invoice.findAllByInvoiceType(prepaymentInvoiceType).each { Invoice prepaymentInvoice ->
            // The amount field will be null for all pre-existing invoices since the field was not being used.
            prepaymentInvoice.invoiceItems.find { it.amount == null }.each { InvoiceItem prepaymentInvoiceItem ->
                prepaymentInvoiceItem.amount = computePrepaymentInvoiceItemAmount(prepaymentInvoiceItem)
                prepaymentInvoiceItem.save(failOnError: true, flush: true)
            }
        }
    }

    // TODO: Can we merge this with PrepaymentInvoiceService? That way if the amount calculation ever changes, it will get picked up automatically here.
    private BigDecimal computePrepaymentInvoiceItemAmount(InvoiceItem prepaymentInvoiceItem) {
        BigDecimal prepaymentPercent = (prepaymentInvoiceItem.order.paymentTerm?.prepaymentPercent ?: Constants.DEFAULT_PAYMENT_PERCENT) / 100

        // If the invoice item is for an adjustment.
        OrderAdjustment orderAdjustment = prepaymentInvoiceItem.orderAdjustment
        if (orderAdjustment) {
            return (orderAdjustment?.canceled ? 0 : orderAdjustment.totalAdjustments) * prepaymentPercent
        }

        // Else if the invoice item is for a canceled order item.
        OrderItem orderItem = prepaymentInvoiceItem.orderItem
        if (orderItem.orderItemStatusCode == OrderItemStatusCode.CANCELED) {
            return 0
        }

        // Else the invoice item is for a non-canceled order item.
        return (orderItem.quantity ?: 0) * (orderItem.unitPrice ?: 0.0) * prepaymentPercent
    }

    /**
     * For STEP 2 of the migration.
     *
     * For every item in a given prepayment invoice, computes the inverse invoice item and insert it to the database
     * by adding it to the given final invoice.
     *
     * This method assumes that because there is both a prepayment invoice and a final invoice, that the order
     * associated with both invoices has been fully invoiced, and so there's no need to check each item of the
     * prepayment invoice individually, they should ALL need to have inverse items created for them.
     */
    void generateInverseInvoiceItems(Invoice prepaymentInvoice, Invoice finalInvoice) {
        prepaymentInvoice.invoiceItems.each { InvoiceItem prepaymentInvoiceItem ->
            List<InvoiceItem> inverseItems = createInverseInvoiceItems(prepaymentInvoiceItem, finalInvoice)
            inverseItems?.each { finalInvoice.addToInvoiceItems(it) }
        }
        finalInvoice.save(failOnError: true, flush: true)
    }

    private List<InvoiceItem> createInverseInvoiceItems(InvoiceItem prepaymentInvoiceItem, Invoice finalInvoice) {
        // If the invoice item is for an adjustment.
        OrderAdjustment orderAdjustment = prepaymentInvoiceItem.orderAdjustment
        if (orderAdjustment) {
            InvoiceItem inverseItem = prepaymentInvoiceService.createInverseItemForOrderAdjustment(orderAdjustment)
            return inverseItem ? [inverseItem] : []
        }

        // Else if the invoice item is for a canceled order item.
        OrderItem orderItem = prepaymentInvoiceItem.orderItem
        if (orderItem.orderItemStatusCode == OrderItemStatusCode.CANCELED) {
            InvoiceItem inverseItem = prepaymentInvoiceService.createInverseItemForCanceledOrderItem(orderItem)
            return inverseItem ? [inverseItem] : []
        }

        // Else the invoice item is for a non-canceled order item. The item might be split across multiple shipments
        // so we need to loop the shipment items and generate an inverse for each.
        // TODO: This was copied from PrepaymentInvoiceService. Why do we need to do this? Shouldn't it be a single inverse for each prepay, regardless of if it was split over multiple shipments?
        List<InvoiceItem> inverseItems = []
        orderItem.shipmentItems?.each { ShipmentItem shipmentItem ->
            // Find the regular invoice item associated with the shipment
            InvoiceItem finalInvoiceItem = finalInvoice.invoiceItems.find{ it.shipmentItem == shipmentItem}

            InvoiceItem inverseItem = prepaymentInvoiceService.createInverseItemForShipmentItem(
                    shipmentItem, finalInvoiceItem)
            if (inverseItem) {
                inverseItems.add(inverseItem)
            }
        }

        return inverseItems
    }
}
