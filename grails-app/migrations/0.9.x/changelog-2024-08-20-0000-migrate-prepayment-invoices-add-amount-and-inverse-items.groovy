import org.pih.warehouse.invoice.Invoice
import org.pih.warehouse.invoice.InvoiceType
import org.pih.warehouse.invoice.InvoiceTypeCode
import org.pih.warehouse.invoice.PrepaymentInvoiceMigrationService
import org.pih.warehouse.order.Order

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
 * More info: https://grails.github.io/grails-database-migration/latest/index.html#_general_format
 */
databaseChangeLog = {

    changeSet(author: "ewaterman", id: "200820240000-0") {

        //noinspection GroovyAssignabilityCheck
        grailsChange {
            //noinspection GroovyAssignabilityCheck
            change {

                PrepaymentInvoiceMigrationService prepaymentInvoiceMigrationService =
                        ctx.getBean("prepaymentInvoiceMigrationService") as PrepaymentInvoiceMigrationService

                /*
                 * STEP 1: Set the amount field for all prepayment invoice items.
                 */
                prepaymentInvoiceMigrationService.updateAmountForPrepaymentInvoiceItems()

                /*
                 * STEP 2: For every pre-existing prepayment invoice, for every invoice item, add an inverse invoice
                 *         item to the final invoice of the order.
                 *
                 * For existing data from before the partial invoicing feature was introduced, ALL non-prepayment
                 * invoices will ALWAYS be the final invoice, meaning every order will have one prepayment invoice
                 * and one non-prepayment (aka final) invoices.
                 */
                int totalOrdersMigrated = 0
                int totalOrdersAlreadyMigrated = 0
                int totalOrdersNotYetFinalInvoiced = 0
                int totalBadOrders = 0
                int totalOrdersWithBadRegInvoice = 0

                InvoiceType prepaymentInvoiceType = InvoiceType.findByCode(InvoiceTypeCode.PREPAYMENT_INVOICE)
                List<Invoice> prepaymentInvoices = Invoice.findAllByInvoiceType(prepaymentInvoiceType)

                int totalPrepaymentOrders = prepaymentInvoices.size()
                prepaymentInvoices.each { Invoice prepaymentInvoice ->
                    // While the domains are structured to allow for multiple orders per invoice, this flow only
                    // makes sense if there's a single order.
                    List<Order> orders = prepaymentInvoice.orders
                    if (!orders || orders.size() != 1) {
                        totalBadOrders++
                        return
                    }
                    Order order = orders[0]

                    // Find the regular invoice that we will be populating with the inverse items. We expect that
                    // there is always only a single (final) invoice. If there is no regular invoice, or it already
                    // has inverse items, skip it.
                    List<Invoice> regularInvoices = order.invoices.findAll { it.isRegularInvoice}
                    if (!regularInvoices) {
                        totalOrdersNotYetFinalInvoiced++
                        return
                    }
                    if (regularInvoices.size() != 1) {
                        totalOrdersWithBadRegInvoice++
                        return
                    }
                    Invoice regularInvoice = regularInvoices[0]
                    if (regularInvoice.invoiceItems.any { it.inverse}) {
                        totalOrdersAlreadyMigrated++
                        return
                    }

                    prepaymentInvoiceMigrationService.generateInverseInvoiceItems(prepaymentInvoice, regularInvoice)

                    totalOrdersMigrated++
                }

                println("Prepayment Invoice migration complete!")
                println("${totalPrepaymentOrders} prepayment invoices/orders were found.")
                println("${totalOrdersMigrated} orders were migrated.")
                println("${totalOrdersAlreadyMigrated} were skipped due to already having been migrated.")
                println("${totalBadOrders} were skipped due to a bad order.")
                println("${totalOrdersWithBadRegInvoice} were skipped due to a bad regular (final) invoice.")
            }
        }
    }
}
