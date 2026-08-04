-- =============================================================================
-- OpenBoxes - Verify reset script table coverage
-- =============================================================================
--
-- Cross-checks every table that reset-transactional-data.sql / .groovy would
-- touch against the target database's actual schema. Any row with a MISSING
-- status means the reset scripts reference a table that does NOT exist in this
-- database, which would abort the reset with an error - fix by removing that
-- table from the scripts (or by pointing the scripts at the right branch).
--
-- USAGE
--   USE vvg_latest;   -- or whichever DB you plan to reset
--   source verify-reset-tables.sql;
--
-- Expected result: every row has status = 'ok'.
-- =============================================================================

SELECT
    t.expected_table,
    IF(i.table_name IS NULL, 'MISSING', 'ok') AS status,
    i.table_type
FROM (
    -- Transactional document content target (subquery in the reset script)
    SELECT 'document' AS expected_table UNION ALL
    -- Cross-reference & attachment join tables
    SELECT 'order_adjustment_invoice'         UNION ALL
    SELECT 'order_invoice'                    UNION ALL
    SELECT 'order_shipment'                   UNION ALL
    SELECT 'shipment_invoice'                 UNION ALL
    SELECT 'fulfillment_item_shipment_item'   UNION ALL
    SELECT 'order_item_comment'               UNION ALL
    SELECT 'order_comment'                    UNION ALL
    SELECT 'shipment_comment'                 UNION ALL
    SELECT 'requisition_comment'              UNION ALL
    SELECT 'order_event'                      UNION ALL
    SELECT 'shipment_event'                   UNION ALL
    SELECT 'requisition_event'                UNION ALL
    SELECT 'order_document'                   UNION ALL
    SELECT 'shipment_document'                UNION ALL
    SELECT 'invoice_document'                 UNION ALL
    SELECT 'shipment_reference_number'        UNION ALL
    SELECT 'invoice_reference_number'         UNION ALL
    SELECT 'requisition_approvers'            UNION ALL
    -- Line / child tables
    SELECT 'order_adjustment'                 UNION ALL
    SELECT 'order_item'                       UNION ALL
    SELECT 'receipt_item'                     UNION ALL
    SELECT 'shipment_item'                    UNION ALL
    SELECT 'container'                        UNION ALL
    SELECT 'requisition_item'                 UNION ALL
    SELECT 'fulfillment_item'                 UNION ALL
    SELECT 'picklist_item'                    UNION ALL
    SELECT 'invoice_item'                     UNION ALL
    SELECT 'cycle_count_item'                 UNION ALL
    -- putaway_task is a VIEW over order_item (PUTAWAY_ORDER), not a table to reset.
    SELECT 'transaction_entry'                UNION ALL
    -- Parent / root records
    SELECT 'receipt'                          UNION ALL
    SELECT 'shipment'                         UNION ALL
    SELECT 'order'                            UNION ALL
    SELECT 'requisition'                      UNION ALL
    SELECT 'fulfillment'                      UNION ALL
    SELECT 'picklist'                         UNION ALL
    SELECT 'invoice'                          UNION ALL
    SELECT 'cycle_count'                      UNION ALL
    SELECT 'cycle_count_request'              UNION ALL
    SELECT 'transaction_source'               UNION ALL
    SELECT 'transaction'                      UNION ALL
    SELECT 'local_transfer'                   UNION ALL
    -- Shared content tables used only by transactional records
    SELECT 'comment'                          UNION ALL
    SELECT 'event'                            UNION ALL
    SELECT 'reference_number'                 UNION ALL
    -- Derived / reporting tables (rebuilt by scheduled jobs)
    SELECT 'product_availability'             UNION ALL
    SELECT 'inventory_snapshot'               UNION ALL
    SELECT 'inventory_item_snapshot'          UNION ALL
    SELECT 'transaction_fact'                 UNION ALL
    SELECT 'consumption_fact'                 UNION ALL
    SELECT 'stockout_fact'                    UNION ALL
    SELECT 'consumption'                      UNION ALL
    SELECT 'date_dimension'                   UNION ALL
    SELECT 'location_dimension'               UNION ALL
    SELECT 'lot_dimension'                    UNION ALL
    SELECT 'product_dimension'                UNION ALL
    SELECT 'transaction_type_dimension'       UNION ALL
    SELECT 'order_summary_mv'
) t
LEFT JOIN information_schema.tables i
    ON i.table_schema = DATABASE()
   AND i.table_name  = t.expected_table
ORDER BY status DESC, t.expected_table;
