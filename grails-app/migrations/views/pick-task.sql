CREATE OR REPLACE VIEW pick_task AS
SELECT
    pli.id AS id,
    CONCAT('PK-', CRC32(pli.id)) AS identifier,

    r.id AS requisition_id,
    r.request_number AS request_number,
    r.delivery_type_code AS delivery_type_code,
    r.date_requested AS date_requested,
    r.priority AS priority,
    r.requested_by_id AS requested_by_id,
    r.status AS requisition_status,
    r.origin_id AS facility_id,

    ri.id AS requisition_item_id,
    ri.product_id AS product_id,

    pli.bin_location_id AS location_id,
    pli.inventory_item_id AS inventory_item_id,
    pli.quantity AS quantity,
    pli.quantity_picked AS quantity_picked,
    pli.picked_by_id AS picked_by_id,
    pli.date_picked AS date_picked,
    pli.reason_code AS reason_code,

    pli.date_created AS date_created,
    pli.last_updated AS last_updated
FROM picklist_item pli
         JOIN picklist pl ON pl.id = pli.picklist_id
         JOIN requisition r ON r.id = pl.requisition_id
         LEFT JOIN requisition_item ri ON ri.id = pli.requisition_item_id
WHERE r.status = 'PICKING';