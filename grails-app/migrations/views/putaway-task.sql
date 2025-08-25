CREATE OR REPLACE VIEW putaway_task AS
    SELECT
        order_item.id as id,
        order_item.product_id,
        order_item.inventory_item_id,
        `order`.destination_id as facility_id,
        order_item.origin_bin_location_id as location_id,
        null as container_id,
        order_item.destination_bin_location_id as destination_id,
        order_item.quantity,
        `order`.id as putaway_order_id,
        `order_item`.id as putaway_order_item_id,
        `order`.status,
        order_item.date_created,
        order_item.last_updated
    from order_item
             join `order` on `order`.id = order_item.order_id
             join order_type on order_type.id = `order`.order_type_id
    where order_type.code = 'PUTAWAY_ORDER';
