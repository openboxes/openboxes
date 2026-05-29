-- =============================================================================
-- OpenBoxes - Reset transactional data
-- =============================================================================
--
-- Deletes all TRANSACTIONAL data (orders, putaways, requisitions/stock movements,
-- shipments, receipts, picks, invoices, cycle counts and all inventory
-- transactions) so an instance can be returned to an empty operational state,
-- while PRESERVING master, reference and configuration data:
--   * MASTER        - products, categories, organizations, persons, inventory
--                     items (lots)
--   * REFERENCE     - locations, units of measure, product suppliers
--   * CONFIGURATION - users/roles, inventory levels (min/max/reorder),
--                     shipment/order/transaction types, shipment workflows,
--                     product/workflow documents
--   (the master/reference/configuration categories are distinct but sometimes
--    blend together; the common thread is that none of it is transactional.)
--
-- Typical uses: refreshing a UAT or demo environment, preparing a sandbox cloned
-- from production, or onboarding a facility from a copied dataset.
--
-- On-hand stock is derived from inventory transactions, so deleting the
-- transactions zeroes out all stock. The derived/reporting tables cleared at the
-- bottom (product_availability, snapshots, fact/dimension tables, order_summary_mv)
-- are rebuilt automatically by the application's scheduled jobs (or via the
-- "Refresh" actions in the admin UI). Inventory items (lot definitions) are kept;
-- see the optional block at the end if you also want to purge them.
--
-- USAGE
--   1. BACK UP THE DATABASE FIRST. This is irreversible.
--      mysqldump -u <user> -p <dbname> > backup-before-reset.sql
--   2. Run against the target (non-production) database:
--      mysql -u <user> -p <dbname> < reset-transactional-data.sql
--   3. Restart the application (or clear the Hibernate 2nd-level cache) so no
--      stale entities remain cached, then let the refresh jobs rebuild the
--      derived tables.
--
-- NOTES
--   * Foreign key checks are disabled for the duration so deletion order does
--     not matter; they are re-enabled at the end.
--   * Everything runs in a single transaction and is committed only if it all
--     succeeds.
--   * `order` is a reserved word and must stay backtick-quoted.
-- =============================================================================

SET FOREIGN_KEY_CHECKS = 0;
SET autocommit = 0;
START TRANSACTION;

-- -----------------------------------------------------------------------------
-- Transactional document CONTENT only.
-- The shared `document` table is also used by product_document and
-- shipment_workflow_document (master/config), so we delete only the documents
-- attached to orders, shipments and invoices. Run this BEFORE clearing the
-- join tables below so the sub-selects can still resolve the ids.
-- -----------------------------------------------------------------------------
DELETE FROM document
WHERE id IN (SELECT document_id FROM order_document)
   OR id IN (SELECT document_id FROM shipment_document)
   OR id IN (SELECT document_id FROM invoice_document);

-- -----------------------------------------------------------------------------
-- Cross-reference and attachment join tables
-- -----------------------------------------------------------------------------
DELETE FROM order_adjustment_invoice;
DELETE FROM order_invoice;
DELETE FROM order_shipment;
DELETE FROM shipment_invoice;
DELETE FROM fulfillment_item_shipment_item;

DELETE FROM order_item_comment;
DELETE FROM order_comment;
DELETE FROM shipment_comment;
DELETE FROM requisition_comment;

DELETE FROM order_event;
DELETE FROM shipment_event;
DELETE FROM requisition_event;

DELETE FROM order_event_log;
DELETE FROM shipment_event_log;

DELETE FROM order_document;
DELETE FROM shipment_document;
DELETE FROM invoice_document;

DELETE FROM shipment_reference_number;
DELETE FROM invoice_reference_number;

DELETE FROM requisition_approvers;

-- -----------------------------------------------------------------------------
-- Line / child tables
-- -----------------------------------------------------------------------------
DELETE FROM order_adjustment;
DELETE FROM order_item;
DELETE FROM receipt_item;
DELETE FROM shipment_item;
DELETE FROM container;
DELETE FROM requisition_item;
DELETE FROM fulfillment_item;
DELETE FROM picklist_item;
DELETE FROM invoice_item;
DELETE FROM cycle_count_item;
DELETE FROM transaction_entry;

-- -----------------------------------------------------------------------------
-- Parent / root records
-- -----------------------------------------------------------------------------
DELETE FROM receipt;
DELETE FROM shipment;
DELETE FROM `order`;
DELETE FROM requisition;
DELETE FROM fulfillment;
DELETE FROM picklist;
DELETE FROM invoice;
DELETE FROM cycle_count;
DELETE FROM cycle_count_request;
DELETE FROM transaction_source;
DELETE FROM `transaction`;
DELETE FROM local_transfer;

-- -----------------------------------------------------------------------------
-- Shared content tables used ONLY by transactional records (safe to clear fully)
-- -----------------------------------------------------------------------------
DELETE FROM comment;
DELETE FROM event;
DELETE FROM event_log;
DELETE FROM reference_number;

-- -----------------------------------------------------------------------------
-- Derived / reporting tables (rebuilt automatically by scheduled jobs)
-- -----------------------------------------------------------------------------
DELETE FROM product_availability;
DELETE FROM inventory_snapshot;
DELETE FROM inventory_item_snapshot;

DELETE FROM transaction_fact;
DELETE FROM consumption_fact;
DELETE FROM stockout_fact;
DELETE FROM consumption;

DELETE FROM date_dimension;
DELETE FROM location_dimension;
DELETE FROM lot_dimension;
DELETE FROM product_dimension;
DELETE FROM transaction_type_dimension;

-- Materialized order summary (rebuilt by OrderSummaryService.refreshOrderSummary)
DELETE FROM order_summary_mv;

-- -----------------------------------------------------------------------------
-- OPTIONAL: also purge lot definitions (inventory items) created during testing.
-- Leave commented out to keep them. Only delete lots no longer referenced by any
-- remaining record. Master "default" lots will simply be recreated as needed.
-- -----------------------------------------------------------------------------
-- DELETE FROM inventory_item;

COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
SET autocommit = 1;
