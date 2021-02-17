CREATE OR REPLACE VIEW product_snapshot AS (
    SELECT location_id,
           product_id,
           sum(quantity_on_hand) as quantity_on_hand
    FROM product_availability
    GROUP BY location_id, product_id
)
