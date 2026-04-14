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
        COALESCE(r.requested_by_id, s.created_by_id)       AS requested_by_id,
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

    UNION ALL
    -- Return orders with PENDING status do not have a shipment yet, so they would not be picked up by the query above, hence this separate query
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
        NULL                                               AS shipment_id,
        NULL                                               AS requisition_id,
        o.id                                               AS order_id,
        NULL                                               AS stocklist_id,
        o.status                                           AS current_status,
        NULL                                               AS shipment_type_id
    FROM `order` o
    WHERE o.order_type_id = 'RETURN_ORDER'
      AND o.status = 'PENDING';
