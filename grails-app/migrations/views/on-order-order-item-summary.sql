CREATE OR REPLACE VIEW on_order_order_item_summary AS (
SELECT
	product.product_code,
	sum(case when `order`.status != 'PENDING' then order_item.quantity else 0 end)- sum(case when shipment.current_status IN (NULL, 'SHIPPED' , 'PARTIALLY_RECEIVED') then shipment_item.quantity else 0 end) as quantity_ordered_not_shipped,
    null as quantity_shipped_not_received
FROM order_item
JOIN
	product ON order_item.product_id = product.id
JOIN
	`order` ON order_item.order_id = `order`.id
LEFT OUTER JOIN
	order_shipment ON order_shipment.order_item_id = order_item.id
LEFT OUTER JOIN
	shipment_item ON order_shipment.shipment_item_id = shipment_item.id
LEFT OUTER JOIN
	shipment ON shipment_item.shipment_id = shipment.id
WHERE `order`.order_type_code = 'PURCHASE_ORDER'
GROUP BY  product.product_code
)
