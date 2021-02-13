CREATE OR REPLACE VIEW product_summary AS
SELECT
  uuid_short() as id,
  product_availability.location_id,
  product.id as product_id,
  coalesce(sum(quantity_on_hand), 0) as quantity_on_hand,
  coalesce(sum(distinct oos.quantity_ordered_not_shipped), 0) as quantity_on_order_not_shipped,
  coalesce(sum(distinct oss.quantity_shipped_not_received), 0) as quantity_on_order_not_received,
  coalesce(sum(distinct oos.quantity_ordered_not_shipped), 0) + coalesce(sum(distinct oss.quantity_shipped_not_received), 0) as quantity_on_order
FROM product_availability
JOIN product on product_availability.product_id = product.id
LEFT OUTER JOIN on_order_order_item_summary as oos
	on (oos.product_id = product_availability.product_id and oos.destination_id = product_availability.location_id)
LEFT OUTER JOIN on_order_shipment_item_summary as oss
	on (oss.product_id = product_availability.product_id and oss.destination_id = product_availability.location_id)
GROUP BY location_id, product_id;
