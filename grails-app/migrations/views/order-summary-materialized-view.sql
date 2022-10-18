-- Drop mv temp table if somehow it still exists
DROP TABLE IF EXISTS mv_order_summary_temp;
-- Create temp mv table from sql view (to shorten the time when MV is unavailable)
CREATE TABLE mv_order_summary_temp AS SELECT * FROM order_summary;
DROP TABLE IF EXISTS mv_order_summary;
-- Copy data from temp mv table into mv table
CREATE TABLE IF NOT EXISTS mv_order_summary LIKE mv_order_summary_temp;
TRUNCATE mv_order_summary;
INSERT INTO mv_order_summary SELECT * FROM mv_order_summary_temp;
ALTER TABLE mv_order_summary ADD UNIQUE INDEX (id);
-- Cleanup
DROP TABLE IF EXISTS mv_order_summary_temp;
