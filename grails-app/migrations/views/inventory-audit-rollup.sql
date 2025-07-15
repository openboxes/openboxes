-- View: inventory_audit_rollup
-- Purpose: Summarizes inventory adjustments at the transaction level, showing total adjustments per transaction.
CREATE OR REPLACE VIEW inventory_audit_rollup AS
select
    facility_id,
    product_id,
    transaction_id,
    max(facility_name) as facility_name,
    max(product_code) as product_code,
    max(transaction_number) as transaction_number,
    max(transaction_date) as transaction_date,
    sum(quantity_adjusted) as quantity_adjusted,
    max(price_per_unit) as price_per_unit,
    max(abc_class) as abc_class,
    CASE
        WHEN SUM(quantity_adjusted) > 0 THEN 'MORE'
        WHEN SUM(quantity_adjusted) < 0 THEN 'LESS'
        ELSE 'EQUAL'
    END AS variance_type_code
from inventory_audit_details
group by facility_id, product_id, transaction_id;
