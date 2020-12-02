CREATE OR REPLACE VIEW stock_movement_item AS
        SELECT
        requisition_item.id,
        requisition_item.requisition_item_type,
        requisition_item.parent_requisition_item_id,
        requisition_item.requisition_id,
        requisition.origin_id,
        requisition.destination_id,
		requisition_item.product_id,
        product.product_code,
        product.name,
        requisition_item.quantity,
        requisition_item.quantity_canceled,
        requisition_item.order_index,
        requisition_item.cancel_reason_code,
        requisition_item.comment as comments,
        CASE
		WHEN modification_item.quantity THEN modification_item.quantity
		WHEN requisition_item.quantity_canceled = requisition_item.quantity THEN 0
        ELSE null
		END AS quantity_revised
    FROM
        requisition_item
            JOIN
        requisition ON requisition_item.requisition_id = requisition.id
			JOIN
		product ON product.id = requisition_item.product_id
            LEFT OUTER JOIN
		requisition_item as modification_item ON modification_item.parent_requisition_item_id = requisition_item.id
		 AND modification_item.requisition_item_type = 'QUANTITY_CHANGE';

