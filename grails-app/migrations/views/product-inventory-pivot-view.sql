CREATE OR REPLACE VIEW product_inventory_pivot_view AS (
    SELECT product_id,
           location_id,
           sum(previous_quantity) AS previous_quantity,
           sum(current_quantity)  AS current_quantity
    FROM product_inventory_extended_view
    GROUP BY product_id, location_id
)
