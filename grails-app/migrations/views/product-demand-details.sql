DROP TABLE IF EXISTS product_demand_details_tmp;
CREATE TABLE product_demand_details_tmp AS
    SELECT
        request_id,
        request_status,
        request_number,
        date_created,
        date_requested,
        date_issued,
        origin_id,
        origin_name,
        destination_id,
        destination_name,
        request_item_id,
        product_id,
        product_code,
        product_name,
        quantity_requested,
        quantity_canceled,
        quantity_approved,
        quantity_modified,
        quantity_picked,
        quantity_demand,
        reason_code,
        reason_code_classification
    FROM product_demand;
DROP TABLE IF EXISTS product_demand_details;
CREATE TABLE IF NOT EXISTS product_demand_details LIKE product_demand_details_tmp;
TRUNCATE product_demand_details;
INSERT INTO product_demand_details SELECT * FROM product_demand_details_tmp;
ALTER TABLE product_demand_details ADD INDEX (product_id, origin_id, destination_id, date_issued, date_requested);
