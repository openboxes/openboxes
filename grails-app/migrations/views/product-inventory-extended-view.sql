CREATE OR REPLACE VIEW product_inventory_extended_view AS (
    SELECT product_inventory_snapshot_view.product_id                             AS product_id,
           product_inventory_snapshot_view.location_id                            AS location_id,
           product_inventory_snapshot_view.date                                   AS date,
           CASE
               WHEN date = timestamp(current_date)
                   THEN quantity_on_hand END                                      AS previous_quantity,
           CASE
               WHEN date = timestamp(current_date + 1)
                   THEN quantity_on_hand END                                      AS current_quantity
    FROM product_inventory_snapshot_view
)

