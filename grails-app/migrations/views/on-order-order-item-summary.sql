CREATE OR REPLACE VIEW on_order_order_item_summary AS
(
SELECT a.product_id,
       a.destination_id,
       ifnull(sum(a.quantity_ordered) - sum(a.quantity_shipped), 0) as quantity_ordered_not_shipped,
       null                                                         as quantity_shipped_not_received
FROM (
         SELECT product.id                   as product_id,
                order_item.id,
                `order`.destination_id,
                sum(distinct case
                                 when `order`.status != 'PENDING'
                                     then order_item.quantity * order_item.quantity_per_uom
                                 else 0 end) as quantity_ordered,
                sum(case
                        when shipment_item.quantity then shipment_item.quantity
                        else 0 end)          as quantity_shipped,
                null                         as quantity_shipped_not_received
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
         WHERE `order`.order_type_id = 'PURCHASE_ORDER'
           AND order_item.order_item_status_code != 'CANCELED'
         GROUP BY product.product_code, order_item.id, product.name, `order`.destination_id) a
GROUP BY product_id, destination_id
    )
