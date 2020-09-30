CREATE OR REPLACE VIEW on_order_summary AS
(SELECT b.product_code, b.name, b.destination_id, b.quantity_ordered_not_shipped, d.quantity_shipped_not_received FROM (
SELECT a.product_code, a.name, a.destination_id, sum(a.quantity_ordered) - sum(a.quantity_shipped) as quantity_ordered_not_shipped , null as quantity_shipped_not_received FROM (
SELECT
        product.product_code,
        product.name,
        order_item.id,
        `order`.destination_id,
        sum(distinct case when `order`.status != 'PENDING' then order_item.quantity * order_item.quantity_per_uom else 0 end) as quantity_ordered,
        sum(case when shipment_item.quantity then shipment_item.quantity else 0 end) as quantity_shipped,
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
    WHERE `order`.order_type_code = 'PURCHASE_ORDER' AND order_item.order_item_status_code != 'CANCELED'
    GROUP BY  product.product_code, order_item.id, product.name, `order`.destination_id ) a
GROUP BY product_code, name, destination_id ) b
LEFT OUTER JOIN
(
SELECT c.product_code, c.name, c.destination_id, null as quantity_ordered_not_shipped, (sum(c.quantity_shipped) - (sum(c.quantity_received) + sum(c.quantity_canceled))) as quantity_shipped_not_received
FROM (
SELECT
	product.product_code,
    product.name,
    shipment_item.id,
    shipment.destination_id,
    sum(distinct shipment_item.quantity) as quantity_shipped,
	sum(case when receipt.receipt_status_code = 'RECEIVED' then receipt_item.quantity_received else 0 end) as quantity_received,
	sum(case when receipt.receipt_status_code = 'RECEIVED' AND receipt_item.quantity_canceled then receipt_item.quantity_canceled else 0 end) as quantity_canceled
FROM
    shipment_item
		JOIN
	product on product.id = shipment_item.product_id
        JOIN
    shipment ON shipment.id = shipment_item.shipment_id
        LEFT OUTER JOIN
    receipt_item ON shipment_item.id = receipt_item.shipment_item_id AND shipment_item.product_id = receipt_item.product_id
        LEFT OUTER JOIN
    receipt ON receipt.id = receipt_item.receipt_id
WHERE
    shipment.current_status IN (NULL, 'SHIPPED' , 'PARTIALLY_RECEIVED')
GROUP BY product.product_code, product.name, shipment_item.id, shipment.destination_id) c
WHERE c.quantity_received + c.quantity_canceled < c.quantity_shipped
GROUP BY product_code, name, destination_id ) d
ON d.product_code = b.product_code AND d.destination_id = b.destination_id )
UNION
(
SELECT d.product_code, d.name, d.destination_id, b.quantity_ordered_not_shipped, d.quantity_shipped_not_received FROM (
SELECT c.product_code, c.name, c.destination_id, (sum(c.quantity_shipped) - (sum(c.quantity_received) + sum(c.quantity_canceled))) as quantity_shipped_not_received
FROM (
SELECT
	product.product_code,
    product.name,
    shipment_item.id,
    shipment.destination_id,
    sum(distinct shipment_item.quantity) as quantity_shipped,
	sum(case when receipt.receipt_status_code = 'RECEIVED' then receipt_item.quantity_received else 0 end) as quantity_received,
	sum(case when receipt.receipt_status_code = 'RECEIVED' AND receipt_item.quantity_canceled then receipt_item.quantity_canceled else 0 end) as quantity_canceled
FROM
    shipment_item
		JOIN
	product on product.id = shipment_item.product_id
        JOIN
    shipment ON shipment.id = shipment_item.shipment_id
        LEFT OUTER JOIN
    receipt_item ON shipment_item.id = receipt_item.shipment_item_id AND shipment_item.product_id = receipt_item.product_id
        LEFT OUTER JOIN
    receipt ON receipt.id = receipt_item.receipt_id
WHERE
    shipment.current_status IN (NULL, 'SHIPPED' , 'PARTIALLY_RECEIVED')
GROUP BY product.product_code, product.name, shipment_item.id, shipment.destination_id) c
WHERE c.quantity_received + c.quantity_canceled < c.quantity_shipped
GROUP BY product_code, name, destination_id ) d
LEFT OUTER JOIN
(
SELECT a.product_code, a.name, a.destination_id, sum(a.quantity_ordered) - sum(a.quantity_shipped) as quantity_ordered_not_shipped FROM (
SELECT
        product.product_code,
        product.name,
        order_item.id,
        `order`.destination_id,
        sum(distinct case when `order`.status != 'PENDING' then order_item.quantity * order_item.quantity_per_uom else 0 end) as quantity_ordered,
        sum(case when shipment_item.quantity then shipment_item.quantity else 0 end) as quantity_shipped,
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
    WHERE `order`.order_type_code = 'PURCHASE_ORDER' AND order_item.order_item_status_code != 'CANCELED'
    GROUP BY  product.product_code, order_item.id, product.name, `order`.destination_id ) a
GROUP BY product_code, name, destination_id ) b
ON d.product_code = b.product_code AND d.destination_id = b.destination_id)

