CREATE OR REPLACE VIEW inventory_audit_summary AS
select *
from inventory_audit_details
group by product_id
