CREATE OR REPLACE VIEW inventory_counts AS
    -- First, just get all of the baseline transactions and adjustment transactions separately, so we can join them later
    -- Having baselines and adjustments above in the helper views, we can now create pairs, when they appear together
    -- Adjustment transaction is hardcoded to be created 1 second after the baseline, so we can rely on TIMESTAMPDIFF
    WITH baseline_adjustment_matches AS (
        SELECT
            a.transaction_id AS adjustment_id,
            b.transaction_id AS baseline_id,
            a.product_id AS product_id,
            a.facility_id AS facility_id
        FROM adjustment_candidate a
        JOIN inventory_baseline_candidate b
          ON a.product_id = b.product_id
          AND a.inventory_id = b.inventory_id
          AND TIMESTAMPDIFF(SECOND, a.transaction_date, b.transaction_date) = -1
    ),

    -- Case 1: Baseline with adjustment
    -- Having IDs of the pairs baseline + adjustment determined, we can now build the expected response, containing
    -- all needed transaction properties - the main transaction is baseline and we just join the baseline + adjustment candidates
    baseline_with_adjustments AS (
        SELECT
            CRC32(CONCAT(bam.baseline_id, bam.product_id, bam.facility_id)) as id,
            bam.baseline_id AS transaction_id,
            bam.product_id AS product_id,
            bam.facility_id AS facility_id,
            t.transaction_date AS date_recorded,
            'BASELINE_ADJUSTMENT' as inventory_count_type_code,
            bam.adjustment_id as associated_transaction_id
            FROM baseline_adjustment_matches bam
            JOIN transaction t ON bam.baseline_id = t.id
    ),

    -- Case 2: Adjustment without baseline ("alone" adjustment) - e.g. record stock for the first time/adjust inventory
    -- Having already determined the baseline + adjustment pairs, to get the alone adjustments, we just get all the adjustment
    -- transactions and check if such adjustment doesn't already exists in the baseline + adjustment pairs - if it does, it means, it is not "alone" adjustment,
    -- so filter it out
    adjustment_without_baseline AS (
        SELECT
            CRC32(CONCAT(te.transaction_id, ii.product_id, facility.id)) as id,
            te.transaction_id AS transaction_id,
            ii.product_id AS product_id,
            facility.id AS facility_id,
            t.transaction_date AS date_recorded,
            'ADJUSTMENT' as inventory_count_type_code,
            -- Alone adjustment doesn't have any associated transaction (like baseline + adjustment), so hardcode it to NULL
            NULL as associated_transaction_id
        FROM transaction_entry te
        JOIN transaction t ON t.id = te.transaction_id
        JOIN inventory_item ii ON ii.id = te.inventory_item_id
        JOIN location facility ON facility.inventory_id = t.inventory_id
        WHERE t.transaction_type_id = '3' -- adjustments
        AND NOT EXISTS (
              SELECT 1
              FROM baseline_adjustment_matches bam
              WHERE bam.adjustment_id = t.id
              AND bam.product_id = ii.product_id
          )
    ),

    -- Case 2: Baseline without adjustment - e.g. submitting a cycle count with quantityCounted equal to QOH
    -- Having already determined the baseline + adjustment pairs, to get the alone baseline transactions, we just get all the baseline transactions
    -- and check if such baseline doesn't already exists in the baseline + adjustment pairs - if it does, it means, it is not "alone" baseline,
    -- so filter it out
    baseline_without_adjustment AS (
        SELECT
            CRC32(CONCAT(te.transaction_id, ii.product_id, facility.id)) as id,
            te.transaction_id AS transaction_id,
            ii.product_id AS product_id,
            facility.id AS facility_id,
            t.transaction_date AS date_recorded,
            'BASELINE' as inventory_count_type_code,
            -- Alone baseline doesn't have any associated transaction (like baseline + adjustment), so hardcode it to NULL
            NULL as associated_transaction_id
        FROM transaction_entry te
        JOIN transaction t ON t.id = te.transaction_id
        JOIN inventory_item ii ON ii.id = te.inventory_item_id
        JOIN location facility ON facility.inventory_id = t.inventory_id
        WHERE t.transaction_type_id = '12' -- baseline inventory transaction
        AND NOT EXISTS (
              SELECT 1
              FROM baseline_adjustment_matches bam
              WHERE bam.baseline_id = t.id
              AND bam.product_id = ii.product_id
          )
    )

-- In the end make a union of all three subviews - since we separated all possible cases (baseline + adjustment, adjustment, baseline)
-- we can be sure that no duplicates will appear
SELECT * FROM baseline_with_adjustments
UNION
SELECT * FROM adjustment_without_baseline
UNION
SELECT * FROM baseline_without_adjustment
