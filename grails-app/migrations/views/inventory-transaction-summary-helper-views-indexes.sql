CREATE INDEX idx_product_code_inventory on inventory_movement_summary(product_code, inventory_id);

CREATE INDEX idx_product_code_facility on product_inventory_summary(product_code, facility_id, baseline_transaction_date);

CREATE INDEX idx_product_id_facility on product_inventory_summary(product_id, facility_id, baseline_transaction_date);

CREATE INDEX idx_product_inventory_summary_lookup ON product_inventory_summary (product_code, facility_id, baseline_transaction_date DESC);
