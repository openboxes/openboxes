CREATE OR REPLACE VIEW inventory_transactions_summary AS
       WITH product_inventory_summary AS (
           SELECT
              inventory_item.product_id as product_id,
              facility.id as facility_id,
              transaction.id as transaction_id,
              transaction.transaction_date as baseline_transaction_date,
              SUM(transaction_entry.quantity) as quantity_balance
            FROM transaction_entry
            JOIN transaction ON transaction.id = transaction_entry.transaction_id
            JOIN transaction_type ON transaction.transaction_type_id = transaction_type.id
            JOIN location facility ON transaction.inventory_id = facility.inventory_id
            JOIN inventory_item ON inventory_item.id = transaction_entry.inventory_item_id
            WHERE transaction_type.id = '12' -- baseline inventory transaction
            GROUP BY transaction.id
    ),
    -- Helper view for adjustments to query against while searching for the latest baseline transaction
    -- without any adjustment transaction between queried adjustment and the latest baseline
    -- Without the helper view, when making the EXISTS statement explicit, it caused performance issues
    -- because the select was called for each row
    adjustments_candidates AS (
       SELECT ii.id as inventory_item_id,
              t.transaction_date as transaction_date,
              t.inventory_id as inventory_id
           FROM transaction t
                    JOIN transaction_entry te ON te.transaction_id = t.id
                    JOIN inventory_item ii ON ii.id = te.inventory_item_id
           WHERE t.transaction_type_id = '3'
    )

SELECT
    CRC32(CONCAT(transaction.id, inventory_item.product_id, facility.id)) as id,
    transaction.id AS transaction_id,
    inventory_item.product_id as product_id,
    facility.id as facility_id,
    COALESCE(pis.quantity_balance, 0) AS quantity_before,
    COALESCE(pis.quantity_balance, 0) + SUM(transaction_entry.quantity) AS quantity_after,
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
                       -- Since we don't have a way to link the baseline and adjustment transactions we need to
                       -- use a window function to find the previous (the latest) baseline transaction
                       AND pis.baseline_transaction_date = (
                           SELECT MAX(baseline_transaction_date)
                           FROM product_inventory_summary pis2
                           WHERE pis2.product_id = inventory_item.product_id
                             AND pis2.facility_id = facility.id
                             AND pis2.baseline_transaction_date < transaction.transaction_date
                             AND NOT EXISTS (
                               SELECT 1
                               FROM adjustments_candidates ac
                               WHERE ac.inventory_item_id = inventory_item.id
                                 AND ac.inventory_id = transaction.inventory_id
                                 AND ac.transaction_date > pis2.baseline_transaction_date
                                 AND ac.transaction_date < transaction.transaction_date
                           )
                       )
WHERE transaction.transaction_type_id = '3' -- Adjustments
GROUP BY transaction.id, inventory_item.product_id, facility.id;
