-- Using a generic view name in case we decide we need to return more data from transaction
-- tables. However, the name is not that important. We might need to use product availability
CREATE OR REPLACE VIEW product_physical_count_history AS
SELECT
    transaction_candidate.inventory_id,
    transaction_candidate.product_id,
    MAX(transaction_candidate.transaction_date) AS date_counted
FROM (
         -- Adjustment transactions (type 3)
         SELECT
             ac.inventory_id,
             ac.product_id,
             ac.transaction_date
         FROM adjustment_candidate ac

         UNION ALL

         -- Baseline inventory transactions (type 12)
         SELECT
             ibc.inventory_id,
             ibc.product_id,
             ibc.transaction_date
         FROM inventory_baseline_candidate ibc

         UNION ALL

         -- Product inventory transactions (type 11)
         SELECT
             pic.inventory_id,
             pic.product_id,
             pic.transaction_date
         FROM product_inventory_candidate pic
     ) AS transaction_candidate
GROUP BY transaction_candidate.inventory_id, transaction_candidate.product_id;

