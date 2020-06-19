CREATE OR REPLACE VIEW product_snapshot AS
(SELECT location_id, product_code, SUM(DISTINCT quantity_on_hand) AS quantity_on_hand
    FROM inventory_snapshot
    GROUP BY location_id, product_code
)
