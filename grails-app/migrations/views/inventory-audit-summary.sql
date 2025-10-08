-- View: inventory_audit_summary
-- Purpose: Aggregates inventory adjustment data at the product level across all transactions within a reporting period.
-- TODO Not current used and does not map to the InventoryAuditSummary domain yet.
CREATE OR REPLACE VIEW inventory_audit_summary AS
select *
from inventory_audit_rollup
group by facility_id, product_id
