CREATE OR REPLACE VIEW inventory_transactions_summary AS
SELECT
    CRC32(CONCAT(transaction.id, inventory_item.product_id, facility.id)) as id,
    transaction.id AS transaction_id,
    product.id as product_id,
    facility.id as facility_id,
    -- If product inventory summary id is null, it means, that the transaction we query against is a single adjustment
    -- to calculate quantity before for such adjustment (since it is not associated with a baseline transaction),
    -- we need to sum quantity from the latest baseline before this adjustment,
    -- and check if there are more "alone" adjustment between such baseline and the current calculated adjustment
    CASE
        WHEN pis.transaction_id IS NULL THEN (
              COALESCE((
                  -- In 99% scenarios there is going to be only one baseline with a particular transaction_date for a product
                  -- but due to some stale old data, where we could experience duplicate baselines.
                  -- So we take MAX here for two reasons:
                  -- 1) to avoid exception with "Subquery did not return unique result"
                  -- 2) we don't take SUM, but MAX not to double the result
                  SELECT MAX(quantity_balance)
                  FROM product_inventory_summary pis
                  WHERE pis.product_code = product.product_code
                    AND pis.facility_id = facility.id
                    -- Find the latest baseline before the calculated adjustment
                    AND pis.baseline_transaction_date = (
                      SELECT MAX(pis2.baseline_transaction_date)
                      FROM product_inventory_summary pis2
                      WHERE pis2.product_code = product.product_code -- comparing the product_code instead of inventory_item.product_id is intentional to avoid MariaDB bug when accessing the inventory_item.product_id with the table prefix (OBS-1901)
                        AND pis2.facility_id = facility.id
                        AND pis2.baseline_transaction_date < transaction.transaction_date
                    )
              ), 0)
            +
             COALESCE((
                 -- Sum the quantity of all adjustments between the calculated adjustment and the latest baseline
                 SELECT SUM(quantity_sum)
                 FROM inventory_movement_summary
                 WHERE inventory_movement_summary.product_code = product.product_code -- comparing the product_code instead of inventory_item.product_id is intentional to avoid MariaDB bug when accessing the inventory_item.product_id with the table prefix (OBS-1901)
                 AND inventory_movement_summary.inventory_id = transaction.inventory_id
                 -- Condition to find the date range between the latest baseline and the current calculated adjustment
                 AND (
                     (SELECT MAX(baseline_transaction_date)
                      FROM product_inventory_summary
                      WHERE product_inventory_summary.product_code = product.product_code
                        AND product_inventory_summary.facility_id = facility.id
                        AND product_inventory_summary.baseline_transaction_date < transaction.transaction_date
                     ) IS NULL
                         OR inventory_movement_summary.transaction_date > (
                         SELECT MAX(baseline_transaction_date)
                         FROM product_inventory_summary
                         WHERE product_inventory_summary.product_code = product.product_code
                           AND product_inventory_summary.facility_id = facility.id
                           AND product_inventory_summary.baseline_transaction_date < transaction.transaction_date
                         )
                 )

                 AND inventory_movement_summary.transaction_date < transaction.transaction_date
             ), 0)
        )
        -- If we have an associated baseline, we just take its quantity, we don't need to search for anything "between"
        ELSE COALESCE(pis.quantity_balance, 0)
    END AS quantity_before,
     -- The condition for calculate quantity after is just quantity before + current sum of adjustment
    COALESCE((
         SELECT MAX(quantity_balance)
         FROM product_inventory_summary pis
         WHERE pis.product_code = product.product_code -- comparing the product_code instead of inventory_item.product_id is intentional to avoid MariaDB bug when accessing the inventory_item.product_id with the table prefix (OBS-1901)
           AND pis.facility_id = facility.id
           AND pis.baseline_transaction_date = (
             SELECT MAX(pis2.baseline_transaction_date)
             FROM product_inventory_summary pis2
             WHERE pis2.product_code = product.product_code -- comparing the product_code instead of inventory_item.product_id is intentional to avoid MariaDB bug when accessing the inventory_item.product_id with the table prefix (OBS-1901)
               AND pis2.facility_id = facility.id
               AND pis2.baseline_transaction_date < transaction.transaction_date
           )
     ), 0)
     +
    COALESCE((
        SELECT SUM(quantity_sum)
        FROM inventory_movement_summary
        WHERE inventory_movement_summary.product_code = product.product_code -- comparing the product_code instead of inventory_item.product_id is intentional to avoid MariaDB bug when accessing the inventory_item.product_id with the table prefix (OBS-1901)
          AND inventory_movement_summary.inventory_id = transaction.inventory_id
          AND (
            (SELECT MAX(baseline_transaction_date)
             FROM product_inventory_summary
             WHERE product_inventory_summary.product_code = product.product_code
               AND product_inventory_summary.facility_id = facility.id
               AND product_inventory_summary.baseline_transaction_date < transaction.transaction_date
            ) IS NULL
                OR inventory_movement_summary.transaction_date > (
                SELECT MAX(baseline_transaction_date)
                FROM product_inventory_summary
                WHERE product_inventory_summary.product_code = product.product_code
                  AND product_inventory_summary.facility_id = facility.id
                  AND product_inventory_summary.baseline_transaction_date < transaction.transaction_date
                )
            )
          AND inventory_movement_summary.transaction_date < transaction.transaction_date
        ), 0)
    + SUM(transaction_entry.quantity) AS quantity_after,
    SUM(transaction_entry.quantity) AS quantity_difference,
    transaction.transaction_date as date_recorded,
    transaction.created_by_id as recorded_by_id,
    pis.transaction_id as baseline_transaction_id
FROM transaction_entry
         JOIN transaction ON transaction.id = transaction_entry.transaction_id
         JOIN inventory_item ON inventory_item.id = transaction_entry.inventory_item_id
         JOIN product ON inventory_item.product_id = product.id
         JOIN location facility ON facility.inventory_id = transaction.inventory_id
         LEFT JOIN product_inventory_summary pis
               ON pis.product_id = inventory_item.product_id
               AND pis.facility_id = facility.id
               -- An adjustment is treated as associated with the baseline if the time diff between them is 1 second (baseline is created 1 second before the adjustment)
               AND TIMESTAMPDIFF(SECOND, pis.baseline_transaction_date, transaction.transaction_date) = 1
WHERE transaction.transaction_type_id = '3' -- Adjustments
GROUP BY transaction.id, inventory_item.product_id;
