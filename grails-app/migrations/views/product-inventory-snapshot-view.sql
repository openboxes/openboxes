CREATE OR REPLACE VIEW product_inventory_snapshot_view AS (
    SELECT inventory_snapshot.product_id            AS product_id,
           inventory_snapshot.location_id           AS location_id,
           inventory_snapshot.date                  AS date,
           SUM(inventory_snapshot.quantity_on_hand) AS quantity_on_hand
    FROM inventory_snapshot
    WHERE date BETWEEN timestamp(current_date) AND timestamp(current_date + 1)
    GROUP BY date, location_id, product_id
)
