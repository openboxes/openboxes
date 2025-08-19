CREATE TABLE IF NOT EXISTS inventory_movement_summary AS
SELECT
    ii.product_id,
    t.transaction_date,
    p.product_code,
    t.inventory_id,
    t.id AS transaction_id,
    SUM(
        -- If it's outbound (transfer out), negate the quantity, as its entries have positive quantity
        CASE WHEN t.transaction_type_id = '9' THEN -te.quantity ELSE te.quantity END
    ) AS quantity_sum
FROM (
         SELECT id, transaction_date, inventory_id, transaction_type_id
         FROM transaction
         WHERE transaction_type_id IN ('3','8','9')
           AND transaction.order_id IS NULL
     ) t
         JOIN transaction_entry te ON te.transaction_id = t.id
         JOIN inventory_item ii ON ii.id = te.inventory_item_id
         JOIN product p ON p.id = ii.product_id
GROUP BY
    t.id,
    ii.product_id,
    t.transaction_date,
    p.product_code,
    t.inventory_id;

CREATE TABLE IF NOT EXISTS product_inventory_summary AS (
    SELECT
        inventory_item.product_id as product_id,
        product.product_code as product_code,
        facility.id as facility_id,
        transaction.id as transaction_id,
        transaction.transaction_date as baseline_transaction_date,
        SUM(transaction_entry.quantity) as quantity_balance
    FROM transaction_entry
             JOIN transaction ON transaction.id = transaction_entry.transaction_id
             JOIN transaction_type ON transaction.transaction_type_id = transaction_type.id
             JOIN location facility ON transaction.inventory_id = facility.inventory_id
             JOIN inventory_item ON inventory_item.id = transaction_entry.inventory_item_id
             JOIN product ON product.id = inventory_item.product_id
    WHERE transaction_type.id = '12' -- baseline inventory transaction
    GROUP BY
        transaction.id,
        inventory_item.product_id,
        product.product_code,
        facility.id,
        transaction.transaction_date
);
