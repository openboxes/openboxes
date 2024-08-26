package org.pih.warehouse.invoice

import grails.gorm.transactions.Transactional

import org.pih.warehouse.core.BudgetCode
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.GlAccount
import org.pih.warehouse.core.UnitOfMeasure
import org.pih.warehouse.order.OrderAdjustment
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.order.OrderItemStatusCode
import org.pih.warehouse.product.Product
import org.pih.warehouse.shipping.ShipmentItem

/**
 * This service exists for the sole purpose of performing data migrations for changelog 0.9.x/2024-08-20-0000.
 * The logic was only put in a service class so that it could be unit tested. Do not use it in regular flows.
 */
@Transactional
class PrepaymentInvoiceMigrationService {

    /**
     * For STEP 1 of the migration.
     *
     * Set the amount field for all prepayment invoice items that existed before the partial invoicing
     * feature was introduced.
     */
    void updateAmountForPrepaymentInvoiceItems() {
        InvoiceType prepaymentInvoiceType = InvoiceType.findByCode(InvoiceTypeCode.PREPAYMENT_INVOICE)

        Invoice.findAllByInvoiceType(prepaymentInvoiceType).each { Invoice prepaymentInvoice ->
            // The amount field will be null for all pre-existing invoices since the field was not being used.
            prepaymentInvoice.invoiceItems.find { it.amount == null }.each { InvoiceItem prepaymentInvoiceItem ->
                prepaymentInvoiceItem.amount = computePrepaymentInvoiceItemAmount(prepaymentInvoiceItem)
                prepaymentInvoiceItem.save(failOnError: true, flush: true)
            }
        }
    }

    // TODO: use transient field on order instead of this once that code is merged
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
    Invoice generateInverseInvoiceItems(Invoice prepaymentInvoice, Invoice finalInvoice) {
        prepaymentInvoice.invoiceItems.each { InvoiceItem prepaymentInvoiceItem ->
            InvoiceItem inverseItem = createInverseInvoiceItem(prepaymentInvoiceItem)
            finalInvoice.addToInvoiceItems(inverseItem)
        }
        return finalInvoice.save(failOnError: true, flush: true)
    }

    private InvoiceItem createInverseInvoiceItem(InvoiceItem prepaymentInvoiceItem) {
        // Because we already computed the amount field for prepayment invoice items in step 1, and because for
        // existing data there will always be a one-to-one mapping of prepayment invoice item to final invoice item,
        // all we need to do to create the inverse item is copy the prepayment invoice item and inverse the amount.
        InvoiceItem inverseItem = new InvoiceItem()

        inverseItem.inverse = true
        inverseItem.amount = prepaymentInvoiceItem.amount * -1

        inverseItem.invoice = prepaymentInvoiceItem.invoice
        inverseItem.shipmentItems = prepaymentInvoiceItem.shipmentItems
        inverseItem.orderItems = prepaymentInvoiceItem.orderItems
        inverseItem.orderAdjustments = prepaymentInvoiceItem.orderAdjustments
        inverseItem.product = prepaymentInvoiceItem.product

        inverseItem.glAccount = prepaymentInvoiceItem.glAccount
        inverseItem.budgetCode = prepaymentInvoiceItem.budgetCode
        inverseItem.quantity = prepaymentInvoiceItem.quantity
        inverseItem.quantityUom = prepaymentInvoiceItem.quantityUom
        inverseItem.quantityPerUom = prepaymentInvoiceItem.quantityPerUom
        inverseItem.unitPrice = prepaymentInvoiceItem.unitPrice

        return inverseItem
    }
}
