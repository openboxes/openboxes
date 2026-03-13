CREATE OR REPLACE VIEW replenishment_task AS
SELECT
    pli.id AS id,
    CONCAT('PK-', CRC32(pli.id)) AS identifier,

    o.id AS order_id,
    o.order_number AS order_number,
    o.status AS order_status,
    o.order_type_id AS order_type_id,
    o.origin_id AS facility_id,
    o.date_created AS date_requested,
    o.created_by_id AS requested_by_id,

    oi.id AS order_item_id,
    oi.product_id AS product_id,

    pli.bin_location_id AS bin_location_id,
    pli.outbound_container_id AS outbound_container_id,
    pli.staging_location_id AS staging_location_id,
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
         JOIN `order` o ON o.id = pl.order_id
         JOIN order_type ot ON ot.id = o.order_type_id
         LEFT JOIN order_item oi ON oi.id = pli.order_item_id
WHERE ot.order_type_code = 'TRANSFER_ORDER' AND o.status NOT IN ('COMPLETED', 'CANCELED', 'REJECTED');