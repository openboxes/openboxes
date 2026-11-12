-- Keep this view mergeable (no GROUP BY / derived tables / correlated subqueries) so MySQL can push
-- the caller's WHERE / ORDER BY / LIMIT down to the base tables and use their indexes.
-- Order level progress (order_total_task_count, order_open_task_count, order_pick_status_code) and
-- the suggested staging_location are computed per page in PickTaskService.populateDerivedFields().
CREATE OR REPLACE VIEW pick_task AS
SELECT
    pli.id AS id,
    CONCAT('PK-', CRC32(pli.id)) AS identifier,

    r.id AS requisition_id,
    r.request_number AS requisition_number,
    r.delivery_type_code AS delivery_type_code,
    r.date_requested AS date_requested,
    CASE r.delivery_type_code
        WHEN 'PICK_UP' THEN 1
        WHEN 'LOCAL_DELIVERY' THEN 2
        WHEN 'SERVICE' THEN 2
        WHEN 'WILL_CALL' THEN 3
        WHEN 'STOCK_TRANSFER_IBT' THEN 3
        WHEN 'SHIP_TO' THEN 4
        ELSE 5
    END AS priority,
    r.requested_by_id AS requested_by_id,
    r.status AS requisition_status,
    r.origin_id AS facility_id,
    r.type AS requisition_type,

    ri.id AS requisition_item_id,
    ri.product_id AS product_id,

    pli.bin_location_id AS location_id,
    pli.outbound_container_id AS outbound_container_id,
    pli.inventory_item_id AS inventory_item_id,
    pli.quantity AS quantity_required,
    pli.quantity_picked AS quantity_picked,
    pli.assignee_id AS assignee_id,
    pli.date_assigned AS date_assigned,
    pli.date_started AS date_started,
    pli.picked_by_id AS picked_by_id,
    pli.date_picked AS date_picked,
    pli.staged_by_id AS staged_by_id,
    pli.date_staged AS date_staged,
    pli.reason_code AS reason_code,
    CASE `pli`.status
        WHEN 'PENDING' THEN 'PENDING'
        WHEN 'PICKING' THEN 'PICKING'
        WHEN 'PICKED' THEN 'PICKED'
        WHEN 'STAGED' THEN 'STAGED'
        ELSE 'PENDING'
    END AS status,

    pli.date_created AS date_created,
    pli.last_updated AS last_updated
FROM picklist_item pli
         JOIN picklist pl ON pl.id = pli.picklist_id
         JOIN requisition r ON r.id = pl.requisition_id
         LEFT JOIN requisition_item ri ON ri.id = pli.requisition_item_id
WHERE r.status IN ('PICKING', 'PICKED', 'STAGED');
