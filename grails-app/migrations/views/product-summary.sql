CREATE OR REPLACE VIEW product_summary AS
SELECT
  uuid_short() as id,
  location_id,
  product_id,
  sum(quantity_on_hand) as quantity_on_hand
FROM product_availability
GROUP BY location_id, product_id;
