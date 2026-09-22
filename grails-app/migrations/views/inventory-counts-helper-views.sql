DROP TABLE IF EXISTS adjustment_candidate;
CREATE TABLE adjustment_candidate AS
    SELECT te.transaction_id AS transaction_id,
           ii.product_id as product_id,
           t.transaction_date,
           t.inventory_id,
           facility.id as facility_id
    FROM transaction_entry te
             JOIN transaction t ON te.transaction_id = t.id
             JOIN inventory_item ii ON ii.id = te.inventory_item_id
             JOIN location facility ON facility.inventory_id = t.inventory_id
    WHERE t.transaction_type_id = '3' -- adjustments
    GROUP BY
        te.transaction_id,
        ii.product_id,
        t.transaction_date,
        t.inventory_id,
        facility.id;

DROP TABLE IF EXISTS inventory_baseline_candidate;
CREATE TABLE inventory_baseline_candidate AS
    SELECT te.transaction_id AS transaction_id,
           ii.product_id as product_id,
           t.transaction_date,
           t.inventory_id,
           facility.id as facility_id
    FROM transaction_entry te
             JOIN transaction t ON te.transaction_id = t.id
             JOIN inventory_item ii ON ii.id = te.inventory_item_id
             JOIN location facility ON facility.inventory_id = t.inventory_id
    WHERE t.transaction_type_id = '12' -- baseline inventory transaction
    AND (t.comment <> 'Inventory baseline created during old product inventory transactions migration for products that had stock but no inventory baseline transaction as a most recent transaction'
        OR t.comment IS NULL)
    GROUP BY
        te.transaction_id,
        ii.product_id,
        t.transaction_date,
        t.inventory_id,
        facility.id;

-- Helps the equality half of the baseline/adjustment join in inventory_counts. The TIMESTAMPDIFF
-- predicate is not sargable, so only the product_id/inventory_id prefix is used to seek; both tables
-- are indexed with the same column order so either side can drive the join.
CREATE INDEX idx_product_inventory_date
    ON adjustment_candidate (product_id, inventory_id, transaction_date);

CREATE INDEX idx_product_inventory_date
    ON inventory_baseline_candidate (product_id, inventory_id, transaction_date);

-- Helps the by-transaction DELETE and product merge UPDATE that InventoryCountService runs against both
-- tables to maintain them incrementally between rebuilds
CREATE INDEX idx_transaction_id
    ON adjustment_candidate (transaction_id);

CREATE INDEX idx_transaction_id
    ON inventory_baseline_candidate (transaction_id);
