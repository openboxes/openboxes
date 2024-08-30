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

    /*
     * STEP 1: Set the amount field for all prepayment invoice items.
     */
    changeSet(author: "ewaterman", id: "200820240000-0") {

        //noinspection GroovyAssignabilityCheck
        grailsChange {
            //noinspection GroovyAssignabilityCheck
            change {
                PrepaymentInvoiceMigrationService prepaymentInvoiceMigrationService =
                        ctx.getBean("prepaymentInvoiceMigrationService") as PrepaymentInvoiceMigrationService

                prepaymentInvoiceMigrationService.updateAmountForPrepaymentInvoiceItems()
            }
        }
    }

    /*
     * STEP 2: For every pre-existing prepayment invoice, for every invoice item, add an inverse invoice
     *         item to the final invoice of the order.
     *
     * For existing data from before the partial invoicing feature was introduced, ALL non-prepayment
     * invoices will ALWAYS be the final invoice, meaning every order will have one prepayment invoice
     * and one non-prepayment (aka final) invoices.
     */
    changeSet(author: "ewaterman", id: "200820240000-1") {

        //noinspection GroovyAssignabilityCheck
        grailsChange {
            //noinspection GroovyAssignabilityCheck
            change {
                long startTime = System.currentTimeMillis()

                PrepaymentInvoiceMigrationService prepaymentInvoiceMigrationService =
                        ctx.getBean("prepaymentInvoiceMigrationService") as PrepaymentInvoiceMigrationService

                int totalOrdersMigrated = 0
                int totalOrdersAlreadyMigrated = 0
                int totalOrdersNotYetFinalInvoiced = 0
                int totalBadOrders = 0

                InvoiceType prepaymentInvoiceType = InvoiceType.findByCode(InvoiceTypeCode.PREPAYMENT_INVOICE)
                List<Invoice> prepaymentInvoices = Invoice.findAllByInvoiceType(prepaymentInvoiceType)

                int totalPrepaymentOrders = prepaymentInvoices.size()
                for (Invoice prepaymentInvoice : prepaymentInvoices) {
                    // While the domains are structured to allow for multiple orders per invoice, this flow only
                    // makes sense if there's a single order.
                    List<Order> orders = prepaymentInvoice.orders
                    if (!orders || orders.size() != 1) {
                        totalBadOrders++
                        return
                    }
                    Order order = orders[0]

                    // Find the regular invoice that we will be populating with the inverse items. For pre-exisitng
                    // data, there is always only a single (final) invoice. If there is no regular invoice, or it
                    // already has inverse items, skip it.
                    List<Invoice> regularInvoices = order.invoices.findAll { it.isRegularInvoice }
                    if (!regularInvoices) {
                        totalOrdersNotYetFinalInvoiced++
                        return
                    }
                    Invoice regularInvoice = regularInvoices[0]
                    if (regularInvoices.size() > 1 || regularInvoice.invoiceItems.any { it.inverse }) {
                        totalOrdersAlreadyMigrated++
                        return
                    }

                    prepaymentInvoiceMigrationService.generateInverseInvoiceItems(prepaymentInvoice, regularInvoice)

                    totalOrdersMigrated++
                }

                long durationInMillis = System.currentTimeMillis() - startTime

                println("Prepayment Invoice migration completed in ${durationInMillis} milliseconds!")
                println("${totalPrepaymentOrders} prepayment invoices/orders were found.")
                println("${totalOrdersMigrated} orders were migrated.")
                println("${totalOrdersAlreadyMigrated} were skipped due to no migration being needed")
                println("${totalBadOrders} were skipped due to a bad order.")
            }
        }
    }
}
