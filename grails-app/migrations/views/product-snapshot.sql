CREATE OR REPLACE VIEW product_snapshot AS (
    SELECT location_id,
           product_id,
           date,
           SUM(quantity_on_hand) AS quantity_on_hand
    FROM inventory_snapshot
    WHERE date = (select max(date) from inventory_snapshot)
    GROUP BY location_id, product_id, date
)
