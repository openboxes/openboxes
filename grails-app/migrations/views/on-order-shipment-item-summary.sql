CREATE OR REPLACE VIEW on_order_shipment_item_summary AS
(
SELECT c.product_id,
       c.destination_id,
       null      as quantity_ordered_not_shipped,
       ifnull((sum(c.quantity_shipped) -
               (sum(c.quantity_received) + sum(c.quantity_canceled))),
              0) as quantity_shipped_not_received
FROM (
         SELECT product.id                           as product_id,
                shipment_item.id,
                shipment.destination_id,
                sum(distinct shipment_item.quantity) as quantity_shipped,
                sum(case
                        when receipt.receipt_status_code = 'RECEIVED'
                            then receipt_item.quantity_received
                        else 0 end)                  as quantity_received,
                sum(case
                        when receipt.receipt_status_code = 'RECEIVED' AND
                             receipt_item.quantity_canceled then receipt_item.quantity_canceled
                        else 0 end)                  as quantity_canceled
         FROM shipment_item
                  JOIN
              product on product.id = shipment_item.product_id
                  JOIN
              shipment ON shipment.id = shipment_item.shipment_id
                  JOIN
              location on location.id = shipment.destination_id
                  JOIN
              location_type on location.location_type_id = location_type.id
                  LEFT OUTER JOIN
              receipt_item ON shipment_item.id = receipt_item.shipment_item_id
                  LEFT OUTER JOIN
              receipt ON receipt.id = receipt_item.receipt_id
         WHERE shipment.current_status IN (NULL, 'SHIPPED', 'PARTIALLY_RECEIVED')
           AND location_type.location_type_code = 'DEPOT'
         GROUP BY product.id, shipment_item.id, shipment.destination_id) c
WHERE c.quantity_received + c.quantity_canceled < c.quantity_shipped
GROUP BY product_id, destination_id
    )
