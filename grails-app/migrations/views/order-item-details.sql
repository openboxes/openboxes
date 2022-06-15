CREATE OR REPLACE VIEW order_item_details AS (
    SELECT
        o_i.id AS id,
        o_i.id AS order_item_id,
        o.id AS order_id,
        o.order_number AS order_number,
        o_i.product_id AS product_id,
        o_i.quantity AS quantity,
        o_i.order_item_status_code AS order_item_status,
        quantity_uom_id,
        quantity_per_uom,
        unit_price
    FROM order_item o_i
        JOIN `order` o ON o.id = o_i.order_id
    WHERE o.order_type_id = 'PURCHASE_ORDER' AND o_i.order_item_status_code != 'CANCELLED'
);
