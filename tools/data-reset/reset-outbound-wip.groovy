/*
 * =============================================================================
 * OpenBoxes - Reset OUTBOUND work-in-progress for a facility
 * =============================================================================
 *
 * Paste into the OpenBoxes admin console. For a given facility, deletes all
 * work-in-progress outbound records so a UAT session can start fresh on the
 * outbound side. Inventory (on-hand stock, transactions, product_availability)
 * is left INTACT so testers still have stock to work with.
 *
 * Scope of "outbound WIP" at facility X:
 *   - Orders where origin = X, destination != X, status is OPEN (PENDING,
 *     APPROVED, PLACED, PARTIALLY_RECEIVED) and orderType is one of:
 *       TRANSFER_ORDER          -> outbound transfer
 *       RETURN_ORDER            -> outbound return
 *   - Requisitions (stock movements) where origin = X, destination != X, and
 *     status is in RequisitionStatus.listPending() (CREATED, EDITING, VERIFYING,
 *     PICKING, PICKED, STAGED, CHECKING, PENDING, REQUESTED, APPROVED,
 *     PENDING_APPROVAL)
 *   - Shipments where origin = X and currentStatus in (CREATED, PENDING,
 *     SHIPPED, PARTIALLY_RECEIVED) -- everything except fully RECEIVED
 *   - Their shipment items, containers, order items, order adjustments,
 *     requisition items, picklists, picklist items, fulfillments, and attached
 *     comment/event/document/reference joins
 *
 * NOT touched:
 *   - Inventory (product_availability, snapshots, transactions, on-hand stock)
 *   - Any record already COMPLETED / RECEIVED / ISSUED / CANCELED
 *   - Purchase orders, putaways, incoming shipments (see reset-inbound-wip.groovy)
 *   - Master, reference, and configuration data
 *
 * SAFETY
 *   - DRY_RUN = true by default; prints the counts that WOULD be deleted.
 *   - FACILITY must be set to a valid location id (UUID) or exact location name.
 *   - Runs inside a single DB transaction; foreign key checks are disabled for
 *     the duration so cross-reference join rows can be cleaned in any order.
 * =============================================================================
 */

import org.pih.warehouse.core.Location
import org.pih.warehouse.inventory.Transaction

// ---- configuration ---------------------------------------------------------
String  FACILITY = ''      // <-- REQUIRED: Location id (UUID) or exact name
boolean DRY_RUN  = true    // <-- set to false to actually delete
// ---------------------------------------------------------------------------

Location facility = FACILITY ? (Location.get(FACILITY) ?: Location.findByName(FACILITY)) : null
if (!facility) {
    println "ERROR: FACILITY must be set to a valid Location id or name. Got: '${FACILITY}'"
    return
}
println "Facility: ${facility.name} (${facility.id})"

def sessionFactory = ctx.sessionFactory
def session = sessionFactory.currentSession

// ---- WIP status sets ------------------------------------------------------
def openOrderStatuses  = ['PENDING', 'APPROVED', 'PLACED', 'PARTIALLY_RECEIVED']
def openReqStatuses    = ['CREATED', 'EDITING', 'VERIFYING', 'PICKING', 'PICKED',
                          'STAGED', 'CHECKING', 'PENDING', 'REQUESTED', 'APPROVED',
                          'PENDING_APPROVAL']
def openShipStatuses   = ['CREATED', 'PENDING', 'SHIPPED', 'PARTIALLY_RECEIVED']

// ---- Identify the WIP outbound record IDs --------------------------------
def orderIds = session.createSQLQuery("""
    SELECT o.id FROM `order` o
    JOIN order_type ot ON ot.id = o.order_type_id
    WHERE o.origin_id = :fid
      AND (o.destination_id IS NULL OR o.destination_id <> :fid)
      AND o.status IN (:openOrderStatuses)
      AND (
           (ot.order_type_code = 'TRANSFER_ORDER')
        OR (ot.code            = 'RETURN_ORDER')
      )
""").setParameter('fid', facility.id).setParameterList('openOrderStatuses', openOrderStatuses).list() as List

def requisitionIds = session.createSQLQuery("""
    SELECT id FROM requisition
    WHERE origin_id = :fid
      AND (destination_id IS NULL OR destination_id <> :fid)
      AND status IN (:openReqStatuses)
""").setParameter('fid', facility.id).setParameterList('openReqStatuses', openReqStatuses).list() as List

def shipmentIds = session.createSQLQuery("""
    SELECT id FROM shipment
    WHERE origin_id = :fid
      AND current_status IN (:openShipStatuses)
""").setParameter('fid', facility.id).setParameterList('openShipStatuses', openShipStatuses).list() as List

// ---- Report --------------------------------------------------------------
println "\n=== Outbound WIP at ${facility.name} ==="
println "  Orders (outbound returns/transfers):               ${orderIds.size()}"
println "  Requisitions (outbound stock movements):           ${requisitionIds.size()}"
println "  Shipments (outgoing, not RECEIVED):                ${shipmentIds.size()}"

if (DRY_RUN) {
    println "\n=== DRY RUN - no data will be deleted. Set DRY_RUN = false to run. ==="
    return
}

if (!orderIds && !requisitionIds && !shipmentIds) {
    println "\nNothing to delete."
    return
}

// ---- Delete atomically with FK checks off --------------------------------
Transaction.withTransaction { status ->
    try {
        session.doWork({ java.sql.Connection connection ->
            java.sql.Statement stmt = connection.createStatement()
            try {
                stmt.execute("SET FOREIGN_KEY_CHECKS = 0")

                def toInList = { List ids -> ids ? ids.collect { "'${it}'" }.join(',') : "''" }

                long total = 0
                def run = { String sql ->
                    int n = stmt.executeUpdate(sql)
                    if (n > 0) { println "    ${n}\t${sql.take(90).replaceAll('\\s+', ' ')}..." }
                    total += n
                }

                // Guarded delete: skip statements whose table/column is absent from
                // this schema (e.g. the optional, legacy fulfillment tables whose
                // structure differs from the current domain model) so the run
                // doesn't error out.
                def columnExists = { String table, String column ->
                    def rs = stmt.executeQuery(
                        "SELECT COUNT(*) FROM information_schema.columns " +
                        "WHERE table_schema = DATABASE() AND table_name = '${table}' AND column_name = '${column}'")
                    try { rs.next(); rs.getInt(1) > 0 } finally { rs.close() }
                }
                def runIfColumn = { String table, String column, String sql -> if (columnExists(table, column)) run(sql) }

                // ---- Orders (outbound returns / outbound transfers) ----
                if (orderIds) {
                    String oIds = toInList(orderIds)
                    run "DELETE oi_inv FROM order_invoice oi_inv JOIN order_item oi ON oi.id = oi_inv.order_item_id WHERE oi.order_id IN (${oIds})"
                    run "DELETE os     FROM order_shipment os    JOIN order_item oi ON oi.id = os.order_item_id       WHERE oi.order_id IN (${oIds})"
                    run "DELETE oic    FROM order_item_comment oic JOIN order_item oi ON oi.id = oic.order_item_comments_id WHERE oi.order_id IN (${oIds})"
                    run "DELETE oai    FROM order_adjustment_invoice oai JOIN order_adjustment oa ON oa.id = oai.order_adjustment_id WHERE oa.order_id IN (${oIds})"
                    run "DELETE FROM order_comment  WHERE order_comments_id  IN (${oIds})"
                    run "DELETE FROM order_document WHERE order_documents_id IN (${oIds})"
                    run "DELETE FROM order_event    WHERE order_events_id    IN (${oIds})"
                    run "DELETE pi FROM picklist_item pi JOIN picklist p ON p.id = pi.picklist_id WHERE p.order_id IN (${oIds})"
                    run "DELETE FROM picklist WHERE order_id IN (${oIds})"
                    run "DELETE FROM order_adjustment WHERE order_id IN (${oIds})"
                    run "DELETE FROM order_item       WHERE order_id IN (${oIds})"
                    run "DELETE FROM `order`          WHERE id       IN (${oIds})"
                }

                // ---- Shipments (outgoing, not RECEIVED) ----
                if (shipmentIds) {
                    String sIds = toInList(shipmentIds)
                    run "DELETE si_inv FROM shipment_invoice si_inv JOIN shipment_item si ON si.id = si_inv.shipment_item_id WHERE si.shipment_id IN (${sIds})"
                    runIfColumn 'fulfillment_item_shipment_item', 'shipment_item_id', "DELETE fs FROM fulfillment_item_shipment_item fs JOIN shipment_item si ON si.id = fs.shipment_item_id WHERE si.shipment_id IN (${sIds})"
                    run "DELETE os     FROM order_shipment os JOIN shipment_item si ON si.id = os.shipment_item_id WHERE si.shipment_id IN (${sIds})"
                    run "DELETE ri FROM receipt_item ri JOIN receipt r ON r.id = ri.receipt_id WHERE r.shipment_id IN (${sIds})"
                    run "DELETE FROM receipt WHERE shipment_id IN (${sIds})"
                    run "DELETE FROM shipment_comment          WHERE shipment_comments_id           IN (${sIds})"
                    run "DELETE FROM shipment_document         WHERE shipment_documents_id          IN (${sIds})"
                    run "DELETE FROM shipment_event            WHERE shipment_events_id             IN (${sIds})"
                    run "DELETE FROM shipment_reference_number WHERE shipment_reference_numbers_id  IN (${sIds})"
                    run "DELETE FROM container     WHERE shipment_id IN (${sIds})"
                    run "DELETE FROM shipment_item WHERE shipment_id IN (${sIds})"
                    run "DELETE FROM shipment      WHERE id IN (${sIds})"
                }

                // ---- Requisitions (outbound stock movements) ----
                if (requisitionIds) {
                    String rIds = toInList(requisitionIds)
                    run "DELETE FROM requisition_approvers WHERE requisition_id IN (${rIds})"
                    run "DELETE FROM requisition_comment   WHERE requisition_id IN (${rIds})"
                    run "DELETE FROM requisition_event     WHERE requisition_id IN (${rIds})"
                    // Fulfillments and their items. The legacy fulfillment table may
                    // exist without a requisition_id column (feature unused / not
                    // migrated), so guard on the column, not just the table.
                    runIfColumn 'fulfillment', 'requisition_id', "DELETE fi FROM fulfillment_item fi JOIN fulfillment f ON f.id = fi.fulfillment_id WHERE f.requisition_id IN (${rIds})"
                    runIfColumn 'fulfillment', 'requisition_id', "DELETE FROM fulfillment WHERE requisition_id IN (${rIds})"
                    run "DELETE pi FROM picklist_item pi JOIN picklist p ON p.id = pi.picklist_id WHERE p.requisition_id IN (${rIds})"
                    run "DELETE FROM picklist WHERE requisition_id IN (${rIds})"
                    run "DELETE pi FROM picklist_item pi JOIN requisition_item ri ON ri.id = pi.requisition_item_id WHERE ri.requisition_id IN (${rIds})"
                    run "DELETE FROM requisition_item WHERE requisition_id IN (${rIds})"
                    run "DELETE FROM requisition       WHERE id IN (${rIds})"
                }

                stmt.execute("SET FOREIGN_KEY_CHECKS = 1")
                println "\n=== Done. Deleted ${total} rows total. ==="
            } finally {
                stmt.close()
            }
        } as org.hibernate.jdbc.Work)

        println "Restart the application (or clear the Hibernate cache) if you rely on any of these entities being absent from cached queries."
    } catch (Exception e) {
        status.setRollbackOnly()
        println "ERROR - rolled back, no changes made: ${e.message}"
        throw e
    }
}
