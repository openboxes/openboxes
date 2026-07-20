/*
 * =============================================================================
 * OpenBoxes - Reset INBOUND work-in-progress for a facility
 * =============================================================================
 *
 * Paste into the OpenBoxes admin console. For a given facility, deletes all
 * work-in-progress inbound records so a UAT session can start fresh on the
 * inbound side. Inventory (on-hand stock, transactions, product_availability)
 * is left INTACT so testers still have stock to work with.
 *
 * Scope of "inbound WIP" at facility X:
 *   - Orders where destination = X and status is OPEN (PENDING, APPROVED,
 *     PLACED, PARTIALLY_RECEIVED) and orderType is one of:
 *       PURCHASE_ORDER          -> inbound
 *       PUTAWAY_ORDER           -> internal (post-receipt), destination = X
 *       RETURN_ORDER            -> inbound return (destination = X)
 *       TRANSFER_ORDER          -> inbound transfer (destination = X, origin != X)
 *   - Requisitions (stock movements) where destination = X, origin != X, and
 *     status is in RequisitionStatus.listPending() (CREATED, EDITING, VERIFYING,
 *     PICKING, PICKED, STAGED, CHECKING, PENDING, REQUESTED, APPROVED,
 *     PENDING_APPROVAL)
 *   - Shipments where destination = X and currentStatus in (CREATED, PENDING,
 *     SHIPPED, PARTIALLY_RECEIVED) -- everything except fully RECEIVED
 *   - Their receipts, receipt items, containers, shipment items, order items,
 *     order adjustments, requisition items, picklists, picklist items,
 *     fulfillments, and attached comment/event/document/reference joins
 *   - PutawayTasks where facility = X and status.isOpen() (PENDING, STARTED,
 *     IN_PROGRESS)
 *
 * NOT touched:
 *   - Inventory (product_availability, snapshots, transactions, on-hand stock)
 *   - Any record already COMPLETED / RECEIVED / ISSUED / CANCELED
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

// ---- WIP status sets (matches the app's own enums) ------------------------
def openOrderStatuses  = ['PENDING', 'APPROVED', 'PLACED', 'PARTIALLY_RECEIVED']
def openReqStatuses    = ['CREATED', 'EDITING', 'VERIFYING', 'PICKING', 'PICKED',
                          'STAGED', 'CHECKING', 'PENDING', 'REQUESTED', 'APPROVED',
                          'PENDING_APPROVAL']
def openShipStatuses   = ['CREATED', 'PENDING', 'SHIPPED', 'PARTIALLY_RECEIVED']
def openPutawayStates  = ['PENDING', 'STARTED', 'IN_PROGRESS']

// ---- Identify the WIP inbound record IDs ----------------------------------
def orderIds = session.createSQLQuery("""
    SELECT o.id FROM `order` o
    JOIN order_type ot ON ot.id = o.order_type_id
    WHERE o.destination_id = :fid
      AND o.status IN (:openOrderStatuses)
      AND (
           ot.order_type_code = 'PURCHASE_ORDER'
        OR ot.code            = 'PUTAWAY_ORDER'
        OR ot.code            = 'RETURN_ORDER'
        OR (ot.order_type_code = 'TRANSFER_ORDER' AND (o.origin_id IS NULL OR o.origin_id <> :fid))
      )
""").setParameter('fid', facility.id).setParameterList('openOrderStatuses', openOrderStatuses).list() as List

def requisitionIds = session.createSQLQuery("""
    SELECT id FROM requisition
    WHERE destination_id = :fid
      AND (origin_id IS NULL OR origin_id <> :fid)
      AND status IN (:openReqStatuses)
""").setParameter('fid', facility.id).setParameterList('openReqStatuses', openReqStatuses).list() as List

def shipmentIds = session.createSQLQuery("""
    SELECT id FROM shipment
    WHERE destination_id = :fid
      AND current_status IN (:openShipStatuses)
""").setParameter('fid', facility.id).setParameterList('openShipStatuses', openShipStatuses).list() as List

def putawayTaskIds = session.createSQLQuery("""
    SELECT id FROM putaway_task
    WHERE facility_id = :fid
      AND status IN (:openPutawayStates)
""").setParameter('fid', facility.id).setParameterList('openPutawayStates', openPutawayStates).list() as List

// ---- Report --------------------------------------------------------------
println "\n=== Inbound WIP at ${facility.name} ==="
println "  Orders (POs, putaways, inbound returns/transfers): ${orderIds.size()}"
println "  Requisitions (inbound stock movements):            ${requisitionIds.size()}"
println "  Shipments (incoming, not RECEIVED):                ${shipmentIds.size()}"
println "  Putaway tasks (open):                              ${putawayTaskIds.size()}"

if (DRY_RUN) {
    println "\n=== DRY RUN - no data will be deleted. Set DRY_RUN = false to run. ==="
    return
}

if (!orderIds && !requisitionIds && !shipmentIds && !putawayTaskIds) {
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

                // Build a quoted, comma-separated ID list for use in IN (...).
                def toInList = { List ids -> ids ? ids.collect { "'${it}'" }.join(',') : "''" }

                long total = 0
                def run = { String sql ->
                    int n = stmt.executeUpdate(sql)
                    if (n > 0) { println "    ${n}\t${sql.take(90).replaceAll('\\s+', ' ')}..." }
                    total += n
                }

                // ---- Orders (POs / putaways / inbound returns / inbound transfers) ----
                if (orderIds) {
                    String oIds = toInList(orderIds)
                    // Item-level joins (via order_item.order_id -> IDs)
                    run "DELETE oi_inv FROM order_invoice oi_inv JOIN order_item oi ON oi.id = oi_inv.order_item_id WHERE oi.order_id IN (${oIds})"
                    run "DELETE os     FROM order_shipment os    JOIN order_item oi ON oi.id = os.order_item_id       WHERE oi.order_id IN (${oIds})"
                    run "DELETE oic    FROM order_item_comment oic JOIN order_item oi ON oi.id = oic.order_item_comments_id WHERE oi.order_id IN (${oIds})"
                    // Adjustment-level joins
                    run "DELETE oai FROM order_adjustment_invoice oai JOIN order_adjustment oa ON oa.id = oai.order_adjustment_id WHERE oa.order_id IN (${oIds})"
                    // Order-level joins (attachments)
                    run "DELETE FROM order_comment  WHERE order_comments_id  IN (${oIds})"
                    run "DELETE FROM order_document WHERE order_documents_id IN (${oIds})"
                    run "DELETE FROM order_event    WHERE order_events_id    IN (${oIds})"
                    // Putaway tasks tied to a putaway order (belt-and-braces: also filter by facility)
                    run "DELETE FROM putaway_task WHERE putaway_order_id IN (${oIds})"
                    // Picklists rooted at these orders
                    run "DELETE pi FROM picklist_item pi JOIN picklist p ON p.id = pi.picklist_id WHERE p.order_id IN (${oIds})"
                    run "DELETE FROM picklist WHERE order_id IN (${oIds})"
                    // Child rows
                    run "DELETE FROM order_adjustment WHERE order_id IN (${oIds})"
                    run "DELETE FROM order_item       WHERE order_id IN (${oIds})"
                    // Parent
                    run "DELETE FROM `order`          WHERE id       IN (${oIds})"
                }

                // ---- Shipments (incoming, not RECEIVED) ----
                if (shipmentIds) {
                    String sIds = toInList(shipmentIds)
                    // Item-level joins
                    run "DELETE si_inv FROM shipment_invoice si_inv JOIN shipment_item si ON si.id = si_inv.shipment_item_id WHERE si.shipment_id IN (${sIds})"
                    run "DELETE fs     FROM fulfillment_item_shipment_item fs JOIN shipment_item si ON si.id = fs.shipment_item_id WHERE si.shipment_id IN (${sIds})"
                    run "DELETE os     FROM order_shipment os JOIN shipment_item si ON si.id = os.shipment_item_id WHERE si.shipment_id IN (${sIds})"
                    // Receipt items (via receipt.shipment_id)
                    run "DELETE ri FROM receipt_item ri JOIN receipt r ON r.id = ri.receipt_id WHERE r.shipment_id IN (${sIds})"
                    run "DELETE FROM receipt WHERE shipment_id IN (${sIds})"
                    // Shipment-level joins
                    run "DELETE FROM shipment_comment          WHERE shipment_comments_id           IN (${sIds})"
                    run "DELETE FROM shipment_document         WHERE shipment_documents_id          IN (${sIds})"
                    run "DELETE FROM shipment_event            WHERE shipment_events_id             IN (${sIds})"
                    run "DELETE FROM shipment_reference_number WHERE shipment_reference_numbers_id  IN (${sIds})"
                    // Child rows
                    run "DELETE FROM container     WHERE shipment_id IN (${sIds})"
                    run "DELETE FROM shipment_item WHERE shipment_id IN (${sIds})"
                    // Parent
                    run "DELETE FROM shipment      WHERE id IN (${sIds})"
                }

                // ---- Requisitions (inbound stock movements) ----
                if (requisitionIds) {
                    String rIds = toInList(requisitionIds)
                    run "DELETE FROM requisition_approvers WHERE requisition_id IN (${rIds})"
                    run "DELETE FROM requisition_comment   WHERE requisition_id IN (${rIds})"
                    run "DELETE FROM requisition_event     WHERE requisition_id IN (${rIds})"
                    // Fulfillments and their items
                    run "DELETE fi FROM fulfillment_item fi JOIN fulfillment f ON f.id = fi.fulfillment_id WHERE f.requisition_id IN (${rIds})"
                    run "DELETE FROM fulfillment WHERE requisition_id IN (${rIds})"
                    // Picklists rooted at these requisitions
                    run "DELETE pi FROM picklist_item pi JOIN picklist p ON p.id = pi.picklist_id WHERE p.requisition_id IN (${rIds})"
                    run "DELETE FROM picklist WHERE requisition_id IN (${rIds})"
                    // Requisition items (parent of self-referential items and their picklist items)
                    run "DELETE pi FROM picklist_item pi JOIN requisition_item ri ON ri.id = pi.requisition_item_id WHERE ri.requisition_id IN (${rIds})"
                    run "DELETE FROM requisition_item WHERE requisition_id IN (${rIds})"
                    // Parent
                    run "DELETE FROM requisition       WHERE id IN (${rIds})"
                }

                // ---- Standalone open putaway tasks at this facility ----
                if (putawayTaskIds) {
                    String pIds = toInList(putawayTaskIds)
                    run "DELETE FROM putaway_task WHERE id IN (${pIds})"
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
