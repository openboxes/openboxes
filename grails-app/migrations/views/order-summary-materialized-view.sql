-- Drop mv temp table if somehow it still exists
DROP TABLE IF EXISTS order_summary_mv_temp;
-- Create temp mv table from sql view (to shorten the time when MV is unavailable)
CREATE TABLE order_summary_mv_temp AS SELECT * FROM order_summary;
DROP TABLE IF EXISTS order_summary_mv;
-- Copy data from temp mv table into mv table
CREATE TABLE IF NOT EXISTS order_summary_mv LIKE order_summary_mv_temp;
TRUNCATE order_summary_mv;
INSERT INTO order_summary_mv SELECT * FROM order_summary_mv_temp;
ALTER TABLE order_summary_mv ADD UNIQUE INDEX (id);
-- Cleanup
DROP TABLE IF EXISTS order_summary_mv_temp;
