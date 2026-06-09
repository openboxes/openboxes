CREATE OR REPLACE VIEW inbound_stock_movement_list_item AS
    -- Querying from shipment (not requisition) to include purchase orders (POs never have a requisition).
    -- Requisition data is preferred via COALESCE when available.
    SELECT
        COALESCE(r.id, s.id)                               AS id,
        COALESCE(r.name, s.name)                           AS name,
        COALESCE(r.request_number, s.shipment_number)      AS identifier,
        COALESCE(r.description, s.description)             AS description,
        COALESCE(r.origin_id, s.origin_id)                 AS origin_id,
        COALESCE(r.destination_id, s.destination_id)       AS destination_id,
        COALESCE(r.date_created, s.date_created)           AS date_created,
        COALESCE(r.last_updated, s.last_updated)           AS last_updated,
        COALESCE(r.date_requested, s.date_created)         AS date_requested,
        r.requested_by_id                                  AS requested_by_id,
        COALESCE(r.created_by_id, s.created_by_id)         AS created_by_id,
        COALESCE(r.updated_by_id, s.updated_by_id)         AS updated_by_id,
        s.id                                               AS shipment_id,
        r.id                                               AS requisition_id,
        NULL                                               AS order_id,
        r.requisition_template_id                          AS stocklist_id,
        s.current_status,
        s.shipment_type_id
    FROM shipment s
    LEFT JOIN requisition r ON r.id = s.requisition_id
    WHERE NOT EXISTS (
        SELECT 1
        FROM shipment_item si
        JOIN order_shipment os ON os.shipment_item_id = si.id
        JOIN order_item oi ON oi.id = os.order_item_id
        JOIN `order` o ON o.id = oi.order_id
        WHERE si.shipment_id = s.id
          AND o.order_type_id = 'RETURN_ORDER'
    )

    UNION ALL
    -- Some stock movements (such as returns) are based on orders. These stock movements will only have a shipment once
    -- the order is placed. Return orders are excluded from the query above to avoid duplicates.
    SELECT
        o.id,
        o.name,
        o.order_number                                     AS identifier,
        o.description,
        o.origin_id,
        o.destination_id,
        o.date_created,
        o.last_updated,
        o.date_ordered                                     AS date_requested,
        o.ordered_by_id                                    AS requested_by_id,
        o.created_by_id,
        o.updated_by_id,
        s.id                                               AS shipment_id,
        NULL                                               AS requisition_id,
        o.id                                               AS order_id,
        NULL                                               AS stocklist_id,
        -- Even though these are order-based inbounds, current status on SM views is always in relation to the shipment (this field resolves to ShipmentStatusCode).
        s.current_status,
        s.shipment_type_id
    FROM `order` o
    LEFT JOIN order_item oi ON oi.order_id = o.id
    LEFT JOIN order_shipment os ON os.order_item_id = oi.id
    LEFT JOIN shipment_item si ON si.id = os.shipment_item_id
    LEFT JOIN shipment s ON s.id = si.shipment_id
    WHERE o.order_type_id = 'RETURN_ORDER'
    GROUP BY o.id, s.id;
