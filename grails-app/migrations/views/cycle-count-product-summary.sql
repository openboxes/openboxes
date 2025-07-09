CREATE OR REPLACE VIEW cycle_count_product_summary AS
WITH cycle_count_item_variance AS (
    SELECT *
    FROM (
        SELECT cycle_count.id                                                             AS cycle_count_id,
               cycle_count_item.id AS cycle_count_item_id,
               ltp.latest_transaction_date AS transaction_date,
               cycle_count_item.date_counted,
               cycle_count.facility_id,
               cycle_count_item.product_id,
               cycle_count_item.inventory_item_id,
               cycle_count_item.location_id,
               cycle_count_item.quantity_on_hand,
               cycle_count_item.quantity_counted,
               cycle_count_item.discrepancy_reason_code                                   AS variance_reason_code,
               cycle_count_item.comment,
               cycle_count_item.count_index
        FROM cycle_count_item
                 LEFT OUTER JOIN cycle_count ON cycle_count_item.cycle_count_id = cycle_count.id
                 LEFT OUTER JOIN cycle_count_request ON cycle_count.id = cycle_count_request.cycle_count_id
                 INNER JOIN latest_transaction_per_cycle_count ltp ON ltp.cycle_count_id = cycle_count.id
        WHERE cycle_count_request.status = 'COMPLETED'
    ) AS cycle_count_item_variance
),

cycle_count_item_final_count AS (
    SELECT *
    FROM cycle_count_item_variance
    WHERE cycle_count_item_variance.count_index = (
        SELECT MAX(count_index)
        FROM cycle_count_item
                 LEFT OUTER JOIN cycle_count ON cycle_count_item.cycle_count_id = cycle_count.id
        WHERE cycle_count_item.cycle_count_id = cycle_count_item_variance.cycle_count_id
          AND cycle_count_item.product_id = cycle_count_item_variance.product_id
          AND cycle_count_item.inventory_item_id = cycle_count_item_variance.inventory_item_id
          AND cycle_count.facility_id = cycle_count_item_variance.facility_id
    )
)

SELECT
    cycle_count_id,
    transaction_date,
    facility_id,
    product_id,
    SUM(quantity_counted - quantity_on_hand) AS quantity_variance
FROM cycle_count_item_final_count
GROUP BY cycle_count_id, facility_id, product_id;
