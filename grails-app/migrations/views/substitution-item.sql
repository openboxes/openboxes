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
        requisition_item.quantity
    FROM
        requisition_item
            JOIN
        requisition ON requisition_item.requisition_id = requisition.id
            JOIN
        product ON product.id = requisition_item.product_id
    WHERE requisition_item_type = 'SUBSTITUTION';
