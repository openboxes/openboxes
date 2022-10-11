DROP TABLE IF EXISTS mv_order_summary;
CREATE TABLE mv_order_summary AS SELECT * FROM order_summary;
ALTER TABLE mv_order_summary ADD INDEX (id);
