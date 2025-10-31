-- This view is needed because each cycle count can have up to two transactions.
-- By creating this view, we ensure that we only retrieve the latest (most recent) transaction per cycle count.
CREATE OR REPLACE VIEW latest_transaction_per_cycle_count AS
SELECT
    cycle_count_id,
    MAX(transaction_date) AS latest_transaction_date
FROM transaction
WHERE cycle_count_id IS NOT NULL
GROUP BY cycle_count_id;
