CREATE OR REPLACE VIEW edit_page_item AS
    SELECT
        stock_movement_item.id,
        stock_movement_item.requisition_item_type,
        stock_movement_item.parent_requisition_item_id,
        stock_movement_item.requisition_id,
        stock_movement_item.origin_id,
        stock_movement_item.destination_id,
        stock_movement_item.product_id,
        stock_movement_item.product_code,
        stock_movement_item.name,
        stock_movement_item.quantity,
        stock_movement_item.quantity_canceled,
        stock_movement_item.quantity_revised,
        stock_movement_item.order_index as sort_order,
        stock_movement_item.cancel_reason_code,
        product_summary.quantity_on_hand,
        product_stocklist.quantity_demand,
        product_substitution_status.substitution_status
    FROM
        stock_movement_item
            LEFT OUTER JOIN
        product_stocklist ON product_stocklist.product_id = stock_movement_item.product_id
            AND product_stocklist.origin_id = stock_movement_item.origin_id
            LEFT OUTER JOIN
        product_summary ON product_summary.product_id = stock_movement_item.product_id
            AND product_summary.location_id = stock_movement_item.origin_id
			LEFT OUTER JOIN
		product_substitution_status ON product_substitution_status.product_id = stock_movement_item.product_id
			AND product_substitution_status.location_id = stock_movement_item.origin_id;
