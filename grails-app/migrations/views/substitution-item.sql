CREATE OR REPLACE VIEW substitution_item AS
    SELECT
        requisition_item.id,
        requisition_item.parent_requisition_item_id,
        requisition_item.requisition_id,
        requisition.origin_id,
        requisition.destination_id,
        requisition_item.product_id,
        product.product_code,
        product.name,
        requisition_item.order_index as sort_order,
        requisition_item.quantity,
        p_a.quantity_on_hand,
        p_a.quantity_available_to_promise,
        product_stocklist.quantity_demand
    FROM
        requisition_item
            JOIN
        requisition ON requisition_item.requisition_id = requisition.id
            JOIN
        product ON product.id = requisition_item.product_id
            LEFT OUTER JOIN
        product_stocklist ON product_stocklist.product_id = requisition_item.product_id
            AND product_stocklist.origin_id = requisition.origin_id
            LEFT OUTER JOIN (
                SELECT product_availability.product_id as product_id,
                       product_availability.location_id as location_id,
                       ifnull(sum(quantity_on_hand), 0) as quantity_on_hand,
                       ifnull(sum(quantity_available_to_promise), 0) as quantity_available_to_promise
                FROM product_availability
                GROUP BY location_id, product_id
            ) p_a ON p_a.product_id = requisition_item.product_id
            AND p_a.location_id = requisition.origin_id
    WHERE requisition_item_type = 'SUBSTITUTION';
