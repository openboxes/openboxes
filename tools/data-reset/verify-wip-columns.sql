-- =============================================================================
-- OpenBoxes - Verify columns used by the WIP reset scripts
-- =============================================================================
--
-- reset-inbound-wip.groovy and reset-outbound-wip.groovy join and filter on
-- specific columns (e.g. order.destination_id, order_type.order_type_code,
-- putaway_task.putaway_order_id, shipment.current_status). Column names can
-- differ across branches, so this script cross-checks every (table, column)
-- pair the WIP scripts rely on against the target database.
--
-- Table existence is covered by verify-reset-tables.sql; this script is
-- focused specifically on column names.
--
-- USAGE
--   USE openboxes;   -- or whichever DB you plan to reset
--   source verify-wip-columns.sql;
--
-- Expected result: every row has status = 'ok'.
-- =============================================================================

SELECT
    p.expected_table,
    p.expected_column,
    IF(c.column_name IS NULL, 'MISSING', 'ok') AS status
FROM (
    -- order (root)
    SELECT 'order' AS expected_table, 'destination_id'         AS expected_column UNION ALL
    SELECT 'order',                    'origin_id'                                UNION ALL
    SELECT 'order',                    'status'                                   UNION ALL
    SELECT 'order',                    'order_type_id'                            UNION ALL
    -- order_type
    SELECT 'order_type',               'order_type_code'                          UNION ALL
    SELECT 'order_type',               'code'                                     UNION ALL
    -- order child / join tables
    SELECT 'order_item',               'order_id'                                 UNION ALL
    SELECT 'order_adjustment',         'order_id'                                 UNION ALL
    SELECT 'order_invoice',            'order_item_id'                            UNION ALL
    SELECT 'order_shipment',           'order_item_id'                            UNION ALL
    SELECT 'order_shipment',           'shipment_item_id'                         UNION ALL
    SELECT 'order_item_comment',       'order_item_comments_id'                   UNION ALL
    SELECT 'order_adjustment_invoice', 'order_adjustment_id'                      UNION ALL
    SELECT 'order_comment',            'order_comments_id'                        UNION ALL
    SELECT 'order_document',           'order_documents_id'                       UNION ALL
    SELECT 'order_event',              'order_events_id'                          UNION ALL
    -- picklist
    SELECT 'picklist',                 'order_id'                                 UNION ALL
    SELECT 'picklist',                 'requisition_id'                           UNION ALL
    SELECT 'picklist_item',            'picklist_id'                              UNION ALL
    SELECT 'picklist_item',            'requisition_item_id'                      UNION ALL
    -- putaway_task
    SELECT 'putaway_task',             'putaway_order_id'                         UNION ALL
    SELECT 'putaway_task',             'facility_id'                              UNION ALL
    SELECT 'putaway_task',             'status'                                   UNION ALL
    -- requisition
    SELECT 'requisition',              'destination_id'                           UNION ALL
    SELECT 'requisition',              'origin_id'                                UNION ALL
    SELECT 'requisition',              'status'                                   UNION ALL
    SELECT 'requisition_approvers',    'requisition_id'                           UNION ALL
    SELECT 'requisition_comment',      'requisition_id'                           UNION ALL
    SELECT 'requisition_event',        'requisition_id'                           UNION ALL
    SELECT 'requisition_item',         'requisition_id'                           UNION ALL
    -- shipment
    SELECT 'shipment',                 'destination_id'                           UNION ALL
    SELECT 'shipment',                 'origin_id'                                UNION ALL
    SELECT 'shipment',                 'current_status'                           UNION ALL
    SELECT 'shipment_invoice',         'shipment_item_id'                         UNION ALL
    SELECT 'shipment_item',            'shipment_id'                              UNION ALL
    SELECT 'receipt',                  'shipment_id'                              UNION ALL
    SELECT 'receipt_item',             'receipt_id'                               UNION ALL
    SELECT 'shipment_comment',         'shipment_comments_id'                     UNION ALL
    SELECT 'shipment_document',        'shipment_documents_id'                    UNION ALL
    SELECT 'shipment_event',           'shipment_events_id'                       UNION ALL
    SELECT 'shipment_reference_number','shipment_reference_numbers_id'            UNION ALL
    SELECT 'container',                'shipment_id'
) p
LEFT JOIN information_schema.columns c
    ON c.table_schema  = DATABASE()
   AND c.table_name    = p.expected_table
   AND c.column_name   = p.expected_column
ORDER BY status DESC, p.expected_table, p.expected_column;
