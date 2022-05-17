CREATE OR REPLACE VIEW stock_movement_list_item AS
    SELECT 
        r.id,
        r.name,
        r.request_number AS identifier,
        r.description,
        r.origin_id,
        r.destination_id,
        r.date_requested,
        r.date_created,
        r.last_updated,
        r.requested_by_id,
        r.created_by_id,
        r.updated_by_id,
        s.id AS shipment_id,
        s.current_status AS shipment_status,
        CASE
            WHEN r.status IS NULL THEN 'REQUESTING'
            WHEN r.status = 'EDITING' THEN 'REQUESTING'
            WHEN r.status = 'VERIFYING' THEN 'REQUESTED'
            WHEN r.status = 'CHECKING' THEN 'PACKED'
            WHEN r.status = 'ISSUED' THEN 'DISPATCHED'
            ELSE r.status
        END AS status_code,
        r.id AS requisition_id,
        r.status AS status,
        ifnull(r.type, 'DEFAULT') AS request_type,
        r.source_type AS source_type,
        r.requisition_template_id AS stocklist_id,
        NULL AS order_id,
        'STOCK_MOVEMENT' AS stock_movement_type
    FROM
        requisition r
            LEFT JOIN
        shipment s ON s.requisition_id = r.id
    WHERE
        r.is_template IS FALSE 
    UNION ALL SELECT 
        o.id,
        o.name,
        o.order_number AS identifier,
        o.description,
        o.origin_id,
        o.destination_id,
        o.date_ordered AS date_requested,
        o.date_created,
        o.last_updated,
        o.ordered_by_id AS requested_by_id,
        o.created_by_id,
        o.updated_by_id,
        s.id AS shipment_id,
        s.current_status AS shipment_status,
        CASE
            WHEN o.status IS NULL THEN 'PENDING'
            WHEN o.status = 'PENDING' THEN 'PENDING'
            WHEN o.status = 'PLACED' THEN 'REQUESTED'
            WHEN o.status = 'APPROVED' THEN 'PICKING'
            WHEN o.status = 'CANCELED' THEN 'CANCELED'
            # These probably won't be used but we want to cover them just in case
            WHEN o.status = 'PARTIALLY_RECEIVED' THEN 'DISPATCHED'
            WHEN o.status = 'RECEIVED' THEN 'DISPATCHED'
            WHEN o.status = 'COMPLETED' THEN 'DISPATCHED'
            WHEN o.status = 'REJECTED' THEN 'CANCELED'
            ELSE 'PENDING'
        END AS status_code,
        NULL AS requisition_id,
        CASE
            WHEN o.status IS NULL THEN 'PENDING'
            WHEN o.status = 'PENDING' THEN 'PENDING'
            WHEN o.status = 'PLACED' THEN 'REQUESTED'
            WHEN o.status = 'APPROVED' THEN 'PICKING'
            WHEN o.status = 'CANCELED' THEN 'CANCELED'
            # These probably won't be used but we want to cover them just in case
            WHEN o.status = 'PARTIALLY_RECEIVED' THEN 'DISPATCHED'
            WHEN o.status = 'RECEIVED' THEN 'DISPATCHED'
            WHEN o.status = 'COMPLETED' THEN 'DISPATCHED'
            WHEN o.status = 'REJECTED' THEN 'CANCELED'
            ELSE 'PENDING'
        END AS status,
        NULL AS request_type,
        NULL AS source_type,
        NULL AS stocklist_id,
        o.id AS order_id,
        'RETURN_ORDER' AS stock_movement_type
    FROM
        `order` o
            LEFT JOIN
        order_item oi ON oi.order_id = o.id
            LEFT JOIN
        order_shipment os ON os.order_item_id = oi.id
            LEFT JOIN
        shipment_item si ON si.id = os.shipment_item_id
            LEFT JOIN
        shipment s ON s.id = si.shipment_id
    WHERE
        order_type_id = 'RETURN_ORDER'
    GROUP BY o.id , s.id;
