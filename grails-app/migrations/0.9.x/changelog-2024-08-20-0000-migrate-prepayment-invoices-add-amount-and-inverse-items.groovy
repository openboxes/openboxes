import org.pih.warehouse.core.Constants
import org.pih.warehouse.invoice.Invoice
import org.pih.warehouse.invoice.InvoiceItem
import org.pih.warehouse.invoice.InvoiceType
import org.pih.warehouse.invoice.InvoiceTypeCode
import org.pih.warehouse.invoice.PrepaymentInvoiceService
import org.pih.warehouse.order.Order
import org.pih.warehouse.order.OrderAdjustment
import org.pih.warehouse.order.OrderItem
import org.pih.warehouse.order.OrderItemStatusCode
import org.pih.warehouse.shipping.ShipmentItem

/*
 * Migrate existing invoices to conform to storing inverse prepayment invoice items in the database.
 *
 * Note that this operation cannot be rolled back. Once this script runs, it will be impossible to distinguish between
 * old invoices that were upgraded by this script and new ones that were created organically.
 *
 * This script uses Grails Domain objects directly because we're doing complex operations on invoices that have complex
 * relationships to other tables. Doing it this way greatly simplifies things like accessing related invoices.
 * In general we should avoid writing Groovy migrations like this and instead stick with the standard XML format.
 *
 * More info on the structure of this file: https://grails.github.io/grails-database-migration/latest/index.html#_general_format
 */
databaseChangeLog = {

    changeSet(author: "ewaterman", id: "200820240000-0") {

        //noinspection GroovyAssignabilityCheck
        grailsChange {
            //noinspection GroovyAssignabilityCheck
            change {

                PrepaymentInvoiceService prepaymentInvoiceService = ctx.getBean("prepaymentInvoiceService")

                /*
                 * Set the inverse field for all invoice items that existed before the partial invoicing
                 * feature was introduced.
                 *
                 * Any inverse invoice items that are defined in the database will have the inverse field also defined
                 * (before the field was introduced, inverse items were not stored in the database at all and instead
                 * were calculated dynamically when fetched). As such, it's safe to assume that if inverse is not
                 * defined for an invoice item, it's not an inverse invoice item.
                 */
                InvoiceItem.findAllByInverseIsNull().each { invoiceItem ->
                    invoiceItem.inverse = false
                    invoiceItem.save(failOnError: true, flush: true)
                }

                InvoiceType prepaymentInvoiceType = InvoiceType.findByCode(InvoiceTypeCode.PREPAYMENT_INVOICE)

                /*
                 * Set the amount field for all prepayment invoice items that existed before the partial invoicing
                 * feature was introduced. (The amount field will be null for all pre-existing invoices since the field
                 * was not being used.)
                 */
                Invoice.findAllByInvoiceType(prepaymentInvoiceType).each { prepaymentInvoice ->
                    prepaymentInvoice.invoiceItems.each { invoiceItem ->
                        // TODO: This should probably be put in a helper method in PrepaymentInvoiceService
                        BigDecimal prepaymentPercent = (invoiceItem.order.paymentTerm?.prepaymentPercent ?: Constants.DEFAULT_PAYMENT_PERCENT) / 100
                        invoiceItem.amount = invoiceItem.quantity * invoiceItem.unitPrice * prepaymentPercent
                        invoiceItem.save(failOnError: true, flush: true)
                    }
                }

                /*
                 * For every pre-existing prepayment invoice, for every invoice item, add an inverse invoice item
                 * to the final invoice of the order.
                 *
                 * For existing data from before the partial invoicing feature was introduced, ALL non-prepayment
                 * invoices will ALWAYS be the final invoice, meaning every order will have one prepayment invoice
                 * and one non-prepayment (aka final) invoices.
                 */
                Invoice.findAllByInvoiceType(prepaymentInvoiceType).each { prepaymentInvoice ->
                    // While the domains are structured to allow for multiple orders per invoice, this flow only
                    // makes sense if there's a single order.
                    List<Order> orders = prepaymentInvoice.orders
                    if (!orders || orders.size() < 1) {
                        return  // probably error or at least log something
                    }
                    Order order = orders[0]

                    // Find the regular (aka final) invoice that we will be populating with the inverse items.
                    // If there is no regular invoice, or it already has inverse items, return early.
                    Invoice regularInvoice = order.invoices.find {invoice -> invoice.isRegularInvoice}
                    if (!regularInvoice || regularInvoice.invoiceItems.any { regularInvoiceItem -> regularInvoiceItem.inverse}) {
                        return
                    }

                    // Add an inverse item to the regular invoice for every item in the order. We do it by order
                    // (instead of by looping the prepay invoice items) because that's what is done in the normal
                    // flow for generating invoices.
                    // TODO: Try to move this code to PrepaymentInvoiceService and integrate it with the existing code.
                    //       Ideally we can just call prepaymentInvoiceService.createInverseItemsForOrder(order) and
                    //       does everything for us. Alternatively, just
                    order.orderItems.each { OrderItem orderItem ->
                        if (orderItem.orderItemStatusCode == OrderItemStatusCode.CANCELED) {
                            InvoiceItem inverseItem = prepaymentInvoiceService.createInverseItemForCanceledOrderItem(orderItem)
                            if (inverseItem) {
                                regularInvoice.addToInvoiceItems(inverseItem)
                            }
                            return
                        }

                        orderItem?.invoiceableShipmentItems?.each { ShipmentItem shipmentItem ->
                            InvoiceItem regularItem = regularInvoice.invoiceItems.find {ii -> ii.shipmentItem == shipmentItem}  // TODO: does this work?
                            InvoiceItem inverseItem = prepaymentInvoiceService.createInverseItemForShipmentItem(shipmentItem, regularItem)
                            if (inverseItem) {
                                regularInvoice.addToInvoiceItems(inverseItem)
                            }
                        }
                    }

                    order.invoiceableAdjustments.each { OrderAdjustment orderAdjustment ->
                        InvoiceItem inverseItem = prepaymentInvoiceService.createInverseItemForOrderAdjustment(orderAdjustment)
                        if (inverseItem) {
                            regularInvoice.addToInvoiceItems(inverseItem)
                        }
                    }

                    regularInvoice.save(failOnError: true, flush: true)
                }
            }
        }
    }
}