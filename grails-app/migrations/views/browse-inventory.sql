CREATE OR REPLACE VIEW browse_inventory AS
SELECT product.id as product_id, SUM(quantity_on_hand) as quantity_on_hand, location_id  FROM product
LEFT OUTER JOIN product_availability ON product_id = product.id
GROUP BY product.id, location_id
