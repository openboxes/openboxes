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

CREATE INDEX idx_product_inventory_date
    ON adjustment_candidate (product_id, inventory_id, transaction_date);
