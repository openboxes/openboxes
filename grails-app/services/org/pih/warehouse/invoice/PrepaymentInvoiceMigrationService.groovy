package org.pih.warehouse.invoice

import grails.gorm.transactions.Transactional

import org.pih.warehouse.order.OrderAdjustment
import org.pih.warehouse.order.OrderItem
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
     *
     * We don't need to check for canceled items or adjustment during this step because anything that was canceled
     * before the prepayment invoice was created wouldn't be included in the prepayment invoice in the first place.
     */
    void updateAmountForPrepaymentInvoiceItems() {
        InvoiceType prepaymentInvoiceType = InvoiceType.findByCode(InvoiceTypeCode.PREPAYMENT_INVOICE)
        List<Invoice> prepaymentInvoices = Invoice.findAllByInvoiceType(prepaymentInvoiceType)
        for (Invoice prepaymentInvoice : prepaymentInvoices) {
            for (InvoiceItem prepaymentInvoiceItem : prepaymentInvoice.invoiceItems) {
                // The amount field will only be null for pre-existing, non-inversed invoices since the field was
                // not being used before the partial invoicing feature was introduced.
                if (prepaymentInvoiceItem.amount != null) {
                    continue
                }

                prepaymentInvoiceItem.amount = computePrepaymentInvoiceItemAmount(prepaymentInvoiceItem)
                prepaymentInvoiceItem.save(failOnError: true)
            }
        }
    }

    private BigDecimal computePrepaymentInvoiceItemAmount(InvoiceItem prepaymentInvoiceItem) {
        BigDecimal prepaymentPercent = prepaymentInvoiceItem.order.prepaymentPercent

        // If the invoice item is for an adjustment.
        OrderAdjustment orderAdjustment = prepaymentInvoiceItem.orderAdjustment
        if (orderAdjustment) {
            return orderAdjustment.totalAdjustments * prepaymentPercent
        }

        // Else the invoice item is for an order item.
        OrderItem orderItem = prepaymentInvoiceItem.orderItem
        return (orderItem.quantity ?: 0) * (orderItem.unitPrice ?: 0.0) * prepaymentPercent
    }

    /**
     * For STEP 2 of the migration.
     *
     * For every item in a given prepayment invoice, set the amount field and add its inverse invoice items to the
     * final invoice of the order. Notably if there are multiple shipments on a single order item, we'll be creating
     * multiple inverse invoice items, one per shipment. Otherwise it's a one-to-one mapping from item in the
     * prepayment invoice to inverse item in the final invoice.
     *
     * This method assumes that because there is both a prepayment invoice and a final invoice, that the order
     * associated with both invoices has been fully invoiced, and so there's no need to check each item of the
     * prepayment invoice individually, they should ALL need to have inverse items created for them.
     *
     * Additionally, we don't need to check for canceled items or adjustments since the inverse is based entirely
     * off the prepayment line. Even if the items are canceled, we still need to inverse the full prepaid amount.
     */
    Invoice migrateFinalInvoice(Invoice prepaymentInvoice, Invoice finalInvoice) {
        // STEP 2.1 - set amount
        setAmountForFinalInvoiceItems(finalInvoice)

        // STEP 2.2 - create inverse items
        for (InvoiceItem prepaymentInvoiceItem : prepaymentInvoice.invoiceItems) {
            List<InvoiceItem> inverseItems = createInverseInvoiceItems(prepaymentInvoiceItem)

            // GORM requires that the invoice items be added to the invoice one by one in a loop in order
            // to be able to set back-references properly.
            inverseItems.each { finalInvoice.addToInvoiceItems(it) }
        }

        return finalInvoice.save(failOnError: true)
    }

    private void setAmountForFinalInvoiceItems(Invoice finalInvoice) {
        for (InvoiceItem finalInvoiceItem : finalInvoice.invoiceItems) {
            // We shouldn't hit this scenario because inverse items won't be generated yet but check just in case.
            if (finalInvoiceItem.inverse) {
                continue
            }
            // We don't need to explicitly check for canceled items or adjustments because for those, the quantity
            // or unit price are already set to 0 (or null in some rare cases).
            finalInvoiceItem.amount = (finalInvoiceItem.quantity ?: 0) * (finalInvoiceItem.unitPrice ?: 0.0)
        }
    }

    private List<InvoiceItem> createInverseInvoiceItems(InvoiceItem prepaymentInvoiceItem) {
        // If the prepayment item has no associated shipment items, it must be an adjustment or a canceled order item.
        Set<ShipmentItem> shipmentItems = prepaymentInvoiceItem.orderItem?.shipmentItems
        if (!shipmentItems) {
            // Because we already computed the amount field for prepayment invoice items in step 1, and because there is
            // always a one-to-one mapping of prepayment invoice item to final invoice item for pre-existing adjustment
            // items and canceled order items, all we need to do to create the inverse item is copy the prepayment
            // invoice item and inverse the amount.
            InvoiceItem inverseItem = prepaymentInvoiceItem.clone()

            inverseItem.inverse = true
            inverseItem.amount = prepaymentInvoiceItem.amount * -1

            return [inverseItem]
        }

        // Otherwise the order item was shipped normally. Because the item might be split up across multiple shipments,
        // and because we generate one regular invoice item per shipment item, we need to make sure to also create
        // one inverse invoice item for each shipment item.
        List<InvoiceItem> inverseItems = []
        for (ShipmentItem shipmentItem : shipmentItems) {
            // A shipment item has no association to prepayment invoice items and so this loop is only on the regular
            // invoice items associated with the shipment.
            for (InvoiceItem regularInvoiceItem : shipmentItem.invoiceItems) {
                // This call does more work than we need at this point because for pre-migration data we know that there
                // aren't any other invoices or inverses to check against. However, simply calling the service here
                // saves us from needing to duplicate code, and the migration is performant enough for it to be okay.
                InvoiceItem inverseItem = prepaymentInvoiceService.createInverseItemForShipmentItem(shipmentItem, regularInvoiceItem)
                if (inverseItem) {
                    inverseItems.add(inverseItem)
                }
            }
        }
        return inverseItems
    }
}
