CREATE OR REPLACE VIEW on_order_shipment_item_summary AS (
SELECT
	product.product_code,
    null as quantity_ordered_not_shipped,
    sum(distinct shipment_item.quantity) -
    sum(case when receipt.receipt_status_code = 'RECEIVED' then receipt_item.quantity_received else 0 end) +
    sum(case when receipt.receipt_status_code = 'RECEIVED' then receipt_item.quantity_canceled else 0 end) as quantity_shipped_not_received
FROM shipment_item
JOIN
	product ON shipment_item.product_id = product.id
JOIN
	shipment ON shipment_item.shipment_id = shipment.id
LEFT OUTER JOIN
	receipt_item ON shipment_item.id = receipt_item.shipment_item_id AND shipment_item.product_id = receipt_item.product_id
LEFT OUTER JOIN
	receipt ON receipt.id = receipt_item.receipt_id
WHERE
	shipment.current_status IN (NULL, 'SHIPPED' , 'PARTIALLY_RECEIVED')
GROUP BY product.product_code
)
