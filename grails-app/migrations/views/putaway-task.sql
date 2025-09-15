CREATE OR REPLACE VIEW putaway_task AS
    SELECT
        order_item.id as id,
        CONCAT('PT-', CRC32(order_item.id)) as identifier,
        -- FIXME derive status based on order item and order status
        CASE `order_item`.order_item_status_code
            WHEN 'PENDING'   THEN 'PENDING'
            WHEN 'APPROVED'    THEN 'IN_PROGRESS'
            WHEN 'PLACED'       THEN 'IN_TRANSIT'
            WHEN 'COMPLETED' THEN 'COMPLETED'
            WHEN 'CANCELED' THEN 'CANCELED'
            ELSE 'PENDING' END AS status,
        order_item.product_id,
        order_item.inventory_item_id,
        `order`.destination_id as facility_id,
        order_item.origin_bin_location_id as location_id,
        -- FIXME we don't have a way to deal with this at the moment
        --  so we'll need to add a new container location to order item
        order_item.container_location_id as container_id,
        order_item.destination_bin_location_id as destination_id,
        order_item.quantity,
        `order`.id as putaway_order_id,
        `order_item`.id as putaway_order_item_id,
        -- FIXME need to resolve these
        `order`.status as putaway_order_status,
        order_item.order_item_status_code as putaway_order_item_status,
        `order`.approved_by_id as assignee_id,
        `order`.completed_by_id as completed_by_id,
        `order`.ordered_by_id as ordered_by_id,
        -- FIXME this mapping is probably not good enough as it'll lead to
        --  a delay, but we don't need to deal with that right now. Perhaps
        --  this could be represented by date_
        `order`.date_approved as date_started,
        -- FIXME Not sure what needs to go here, but it could be
        --  date_completed IFF the status = CANCELED, null otherwise
        null as date_canceled,
        `order`.date_completed as date_completed,

        order_item.date_created,
        order_item.last_updated
    from order_item
             join `order` on `order`.id = order_item.order_id
             join order_type on order_type.id = `order`.order_type_id
    where order_type.code = 'PUTAWAY_ORDER';
