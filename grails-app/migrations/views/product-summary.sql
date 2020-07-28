CREATE OR REPLACE VIEW product_summary AS
SELECT
  location_id,
  product_id,
  @sum_quantity_on_hand := sum(quantity_on_hand)
FROM inventory_snapshot
WHERE date = date(now())+1
GROUP BY location_id, product_id
ON DUPLICATE KEY
UPDATE quantity_on_hand = sum_quantity_on_hand;
