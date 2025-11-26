-- This table should not have any new records, as product inventory transactions are deprecated.
-- The table is kept for handling the legacy data and improve the performance of cycle count candidate view that
-- relies on the data in this table.
DROP TABLE IF EXISTS product_inventory_candidate;
CREATE TABLE product_inventory_candidate AS
SELECT te.transaction_id AS transaction_id,
       ii.product_id as product_id,
       t.transaction_date,
       t.inventory_id,
       facility.id as facility_id
FROM transaction_entry te
         JOIN transaction t ON te.transaction_id = t.id
         JOIN inventory_item ii ON ii.id = te.inventory_item_id
         JOIN location facility ON facility.inventory_id = t.inventory_id
WHERE t.transaction_type_id = '11' -- product inventory transaction

GROUP BY
    te.transaction_id,
    ii.product_id,
    t.transaction_date,
    t.inventory_id,
    facility.id;

CREATE INDEX idx_inventory_product_date
    ON product_inventory_candidate (inventory_id, product_id, transaction_date);

CREATE INDEX idx_transaction_id
    ON product_inventory_candidate (transaction_id);

CREATE INDEX idx_product_inventory_base
    ON product_inventory_candidate (product_id, inventory_id, transaction_date);
