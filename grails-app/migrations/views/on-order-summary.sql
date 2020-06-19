CREATE OR REPLACE VIEW on_order_summary AS
(SELECT a.product_code, a.name, a.destination_id, a.quantity_ordered_not_shipped, b.quantity_shipped_not_received
    FROM
    (SELECT
        product.product_code,
        product.name,
        `order`.destination_id,
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
    GROUP BY  product.product_code, product.name, `order`.destination_id ) a
    LEFT OUTER JOIN
    (SELECT
        product.product_code,
		product.name,
        shipment.destination_id,
        null,
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
    GROUP BY product.product_code, product.name, shipment.destination_id) b ON a.product_code = b.product_code AND a.destination_id = b.destination_id)
    UNION
    (
    SELECT a.product_code, a.name, a.destination_id, b.quantity_ordered_not_shipped, a.quantity_shipped_not_received
    FROM
    (SELECT
        product.product_code,
        product.name,
        shipment.destination_id,
        null,
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
    GROUP BY product.product_code, product.name, shipment.destination_id) a
    LEFT OUTER JOIN
    (SELECT
        product.product_code,
		product.name,
        `order`.destination_id,
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
    GROUP BY  product.product_code, product.name, `order`.destination_id ) b ON a.product_code = b.product_code AND a.destination_id = b.destination_id
)

