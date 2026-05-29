/*
 * =============================================================================
 * OpenBoxes - Reset transactional data (Grails / OpenBoxes console script)
 * =============================================================================
 *
 * Paste this into the OpenBoxes admin console (Configuration > Console, i.e. the
 * Grails web console) and run it. It deletes all TRANSACTIONAL data so an
 * instance can be returned to an empty operational state, while PRESERVING
 * master, reference and configuration data:
 *   - MASTER        : products, categories, organizations, persons, inventory
 *                     items (lots)
 *   - REFERENCE     : locations, units of measure, product suppliers
 *   - CONFIGURATION : users/roles, inventory levels, shipment/order/transaction
 *                     types, shipment workflows, product/workflow documents
 *   (these three categories are distinct but sometimes blend together; the
 *    common thread is that none of it is transactional.)
 *
 * Typical uses: refreshing a UAT or demo environment, preparing a sandbox cloned
 * from production, or onboarding a facility from a copied dataset.
 *
 * What gets cleared:
 *   - Orders / purchase orders / putaways / transfer orders, order items,
 *     adjustments and their picklists
 *   - Putaway tasks
 *   - Requisitions (stock movements), requisition items, fulfillments
 *   - Shipments, shipment items, containers, reference numbers
 *   - Receipts and receipt items
 *   - Invoices and invoice items
 *   - Cycle counts, cycle count requests and items
 *   - All inventory transactions + entries (this zeroes on-hand stock) and
 *     transaction sources / local transfers
 *   - Comments and events attached to the above
 *   - Documents attached to orders/shipments/invoices (product & workflow
 *     documents are preserved)
 *   - Derived/reporting tables (product availability, snapshots, fact &
 *     dimension tables, materialized order summary) - these are rebuilt by the
 *     application's scheduled refresh jobs.
 *
 * Everything runs inside a SINGLE database transaction: if any statement fails
 * the whole thing rolls back and nothing is changed.
 *
 * IMPORTANT
 *   - BACK UP THE DATABASE BEFORE RUNNING. This is irreversible.
 *   - Run on non-production environments only.
 *   - Raw SQL deletes bypass the Hibernate caches, so RESTART the application
 *     afterwards (or clear the 2nd-level cache) and let the refresh jobs rebuild
 *     the derived tables.
 *   - Set DRY_RUN = true to print the row counts that WOULD be deleted without
 *     changing anything.
 * =============================================================================
 */

import org.pih.warehouse.inventory.Transaction

boolean DRY_RUN = true   // <-- set to false to actually perform the reset

// Tables emptied completely, in an order that is valid even with FK checks off.
def tablesToClear = [
    // cross-reference & attachment join tables
    'order_adjustment_invoice', 'order_invoice', 'order_shipment', 'shipment_invoice',
    'fulfillment_item_shipment_item',
    'order_item_comment', 'order_comment', 'shipment_comment', 'requisition_comment',
    'order_event', 'shipment_event', 'requisition_event',
    'order_document', 'shipment_document', 'invoice_document',
    'shipment_reference_number', 'invoice_reference_number',
    'requisition_approvers',
    // line / child tables
    'order_adjustment', 'order_item', 'receipt_item', 'shipment_item', 'container',
    'requisition_item', 'fulfillment_item', 'picklist_item', 'invoice_item',
    'cycle_count_item', 'putaway_task', 'transaction_entry',
    // parent / root records
    'receipt', 'shipment', '`order`', 'requisition', 'fulfillment', 'picklist',
    'invoice', 'cycle_count', 'cycle_count_request', 'transaction_source',
    '`transaction`', 'local_transfer',
    // shared content tables used only by transactional records
    'comment', 'event', 'reference_number',
    // derived / reporting tables (rebuilt by scheduled jobs)
    'product_availability', 'inventory_snapshot', 'inventory_item_snapshot',
    'transaction_fact', 'consumption_fact', 'stockout_fact', 'consumption',
    'date_dimension', 'location_dimension', 'lot_dimension', 'product_dimension',
    'transaction_type_dimension',
    'order_summary_mv',
]

def sessionFactory = ctx.sessionFactory
def session = sessionFactory.currentSession

def countRows = { String table ->
    session.createSQLQuery("SELECT COUNT(*) FROM ${table}".toString()).uniqueResult() as Long
}

if (DRY_RUN) {
    println "=== DRY RUN - no data will be deleted ==="
    long total = 0
    // The shared `document` table: only transactional docs would be removed.
    Long docCount = session.createSQLQuery(
        "SELECT COUNT(*) FROM document WHERE id IN (SELECT document_id FROM order_document) " +
        "OR id IN (SELECT document_id FROM shipment_document) " +
        "OR id IN (SELECT document_id FROM invoice_document)").uniqueResult() as Long
    println "  document (transactional only): ${docCount}"
    total += docCount
    tablesToClear.each { table ->
        Long c = countRows(table)
        println "  ${table}: ${c}"
        total += c
    }
    println "=== Total rows that WOULD be deleted: ${total} ==="
    println "Set DRY_RUN = false to perform the reset."
    return
}

// Real run - single atomic transaction. All statements execute on the one JDBC
// connection bound to this transaction (via doWork), so SET FOREIGN_KEY_CHECKS
// and the deletes share the same session and commit/roll back together.
Transaction.withTransaction { status ->
    try {
        session.doWork({ java.sql.Connection connection ->
            java.sql.Statement stmt = connection.createStatement()
            try {
                stmt.execute("SET FOREIGN_KEY_CHECKS = 0")

                // Transactional document content only (preserve product/workflow
                // docs). Must run before the document join tables are cleared.
                int docs = stmt.executeUpdate(
                    "DELETE FROM document WHERE id IN (SELECT document_id FROM order_document) " +
                    "OR id IN (SELECT document_id FROM shipment_document) " +
                    "OR id IN (SELECT document_id FROM invoice_document)")
                println "Deleted ${docs} transactional document(s)"

                long total = docs
                tablesToClear.each { table ->
                    int deleted = stmt.executeUpdate("DELETE FROM ${table}".toString())
                    println "Deleted ${deleted} from ${table}"
                    total += deleted
                }

                stmt.execute("SET FOREIGN_KEY_CHECKS = 1")
                println "=== Done. Deleted ${total} rows total. ==="
            } finally {
                stmt.close()
            }
        } as org.hibernate.jdbc.Work)

        println "Restart the application (or clear the Hibernate cache) and allow the " +
                "refresh jobs to rebuild product availability / snapshots / reporting tables."
    } catch (Exception e) {
        status.setRollbackOnly()
        println "ERROR - rolled back, no changes made: ${e.message}"
        throw e
    }
}
